/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.statium

import eu.europa.ec.eudi.statium.cose.ParseCwt
import eu.europa.ec.eudi.statium.http.GetStatusListTokenKtorOps
import eu.europa.ec.eudi.statium.jose.jwtHeaderAndPayload
import eu.europa.ec.eudi.statium.misc.runCatchingCancellable
import io.ktor.client.*
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborLabel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

public fun interface GetStatusListToken {

    public suspend operator fun invoke(uri: String, at: Instant?): Result<StatusListTokenClaims>

    public companion object {
        /**
         * Factory method that creates an instance of [GetStatusListToken]
         * that works with JWT encoded status list tokens
         */
        public fun usingJwt(
            clock: Clock,
            httpClient: HttpClient,
            verifyStatusListTokenSignature: VerifyStatusListTokenJwtSignature,
            allowedClockSkew: Duration = Duration.ZERO,
        ): GetStatusListToken =
            GetStatusListTokenUsingJwt(
                clock,
                httpClient,
                verifyStatusListTokenSignature,
                allowedClockSkew,
            )

        /**
         * Factory method that creates an instance of [GetStatusListToken]
         * that works with CWT encoded status list tokens
         */
        public fun usingCwt(
            clock: Clock,
            httpClient: HttpClient,
            verifyStatusListTokenSignature: VerifyStatusListTokenCwtSignature,
            parseCwt: ParseCwt<ByteArray, ByteArray> = ParseCwt.default(),
            allowedClockSkew: Duration = Duration.ZERO,
        ): GetStatusListToken =
            GetStatusListTokenUsingCwt(
                clock,
                httpClient,
                verifyStatusListTokenSignature,
                parseCwt,
                allowedClockSkew,
            )
    }
}

internal class GetStatusListTokenUsingJwt(
    private val clock: Clock,
    private val httpClient: HttpClient,
    private val verifyJwtSignature: VerifyStatusListTokenJwtSignature,
    private val allowedClockSkew: Duration,
) : GetStatusListToken, GetStatusListTokenKtorOps, StatusListTokenValidations {

    init {
        require(allowedClockSkew >= Duration.ZERO) { "allowedClockSkew must be >= 0" }
    }

    override suspend fun invoke(uri: String, at: Instant?): Result<StatusListTokenClaims> =
        runCatchingCancellable {
            val unverifiedJwt = fetchToken(uri, at)
            val validationTime = at ?: clock.now()
            verifySignature(unverifiedJwt, validationTime)
            val (header, claims) = parse(unverifiedJwt)
            header.ensureTypeIsStatusListJwt()
            claims.ensureValid(expectedSubject = uri, validationTime, allowedClockSkew = allowedClockSkew)
        }

    private suspend fun fetchToken(uri: String, at: Instant?): String =
        httpClient.getStatusListTokenInJwt(uri, at).getOrThrow()

    private suspend fun verifySignature(unverifiedJwt: String, verificationTime: Instant) {
        verifyJwtSignature(unverifiedJwt, verificationTime)
            .getOrElse { error -> throw IllegalStateException("Invalid JWT signature", error) }
    }

    private fun parse(jwt: String): Pair<Header, StatusListTokenClaims> =
        jwtHeaderAndPayload<Header, StatusListTokenClaims>(jwt).getOrThrow()

    private fun Header.ensureTypeIsStatusListJwt() {
        checkNotNull(type) {
            "Missing `typ` from JOSE header"
        }
        val expectedType = TokenStatusListSpec.MEDIA_SUBTYPE_STATUS_LIST_JWT
        check(type == expectedType) {
            "Wrong `typ` expecting $expectedType found $type"
        }
    }

    @Serializable
    private data class Header(
        @SerialName("alg") @Required val algorithm: String,
        @SerialName("typ") val type: String? = null,
    ) {
        init {
            require(algorithm.isNotBlank()) { "alg can't be blank" }
        }
    }
}

internal class GetStatusListTokenUsingCwt(
    private val clock: Clock,
    private val httpClient: HttpClient,
    private val verifyCwtSignature: VerifyStatusListTokenCwtSignature,
    parseCwt: ParseCwt<ByteArray, ByteArray>,
    private val allowedClockSkew: Duration,
) : GetStatusListToken, GetStatusListTokenKtorOps, StatusListTokenValidations {

    init {
        require(allowedClockSkew >= Duration.ZERO) { "allowedClockSkew must be >= 0" }
    }

    private val parseCwt: ParseCwt<ProtectedHeader, StatusListTokenClaims> = ParseCwt.map(parseCwt)

    override suspend fun invoke(uri: String, at: Instant?): Result<StatusListTokenClaims> =
        runCatchingCancellable {
            val unverifiedCwt = fetchToken(uri, at)
            val validationTime = at ?: clock.now()
            verifySignature(unverifiedCwt, validationTime)
            val (header, claims) = parseCwt(unverifiedCwt)
            checkNotNull(claims) { "Missing claims in CWT" }
            header.ensureTypeIsStatusListCwt()
            claims.ensureValid(expectedSubject = uri, validationTime, allowedClockSkew = allowedClockSkew)
        }

    private suspend fun fetchToken(uri: String, at: Instant?): ByteArray =
        httpClient.getStatusListTokenInCwt(uri, at).getOrThrow()

    private suspend fun verifySignature(unverifiedCwt: ByteArray, verificationTime: Instant) {
        verifyCwtSignature(unverifiedCwt, verificationTime)
            .getOrElse { error -> throw IllegalStateException("Invalid CWT signature", error) }
    }

    private fun ProtectedHeader.ensureTypeIsStatusListCwt() {
        checkNotNull(type) {
            "Missing `type` from COSE header"
        }
        val expectedType = TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT
        check(type == expectedType) {
            "Wrong `type (16)` expecting $expectedType found $type"
        }
    }

    @Serializable
    internal data class ProtectedHeader(
        @CborLabel(16) val type: String? = null,
    )
}
