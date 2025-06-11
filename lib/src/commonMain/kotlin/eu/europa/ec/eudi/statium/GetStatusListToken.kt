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

import eu.europa.ec.eudi.statium.http.GetStatusListTokenKtorOps
import eu.europa.ec.eudi.statium.jose.jwtHeaderAndPayload
import io.ktor.client.*
import io.ktor.utils.io.core.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

public fun interface GetStatusListToken {

    public suspend operator fun invoke(uri: String, at: Instant?): Result<StatusListTokenClaims>

    public companion object {
        /**
         * Factory method that creates an instance of [GetStatusListToken]
         */
        public fun usingJwt(
            clock: Clock,
            httpClientFactory: () -> HttpClient,
            verifyStatusListTokenSignature: VerifyStatusListTokenSignature,
            allowedClockSkew: Duration = Duration.ZERO,
        ): GetStatusListToken = GetStatusListTokenUsingJwt(clock, httpClientFactory, verifyStatusListTokenSignature, allowedClockSkew)
    }
}

internal class GetStatusListTokenUsingJwt(
    private val clock: Clock,
    private val httpClientFactory: () -> HttpClient,
    private val verifySignature: VerifyStatusListTokenSignature,
    private val allowedClockSkew: Duration,
) : GetStatusListToken, GetStatusListTokenKtorOps, StatusListTokenValidations {

    init {
        require(allowedClockSkew >= Duration.ZERO) { "allowedClockSkew must be >= 0" }
    }

    override suspend fun invoke(uri: String, at: Instant?): Result<StatusListTokenClaims> =
        runCatching {
            val unverifiedJwt = fetchToken(uri, at)
            val validationTime = at ?: clock.now()
            verifySignature(unverifiedJwt, validationTime)
            val (header, claims) = parse(unverifiedJwt)
            header.ensureTypeIsStatusListJwt()
            claims.ensureValid(expectedSubject = uri, validationTime, allowedClockSkew = allowedClockSkew)
        }

    private suspend fun fetchToken(uri: String, at: Instant?): String =
        httpClientFactory().use { httpClient ->
            httpClient.getStatusListToken(uri, StatusListTokenFormat.JWT, at).getOrThrow()
        }
    private suspend fun verifySignature(unverifiedJwt: String, verificationTime: Instant) {
        verifySignature(unverifiedJwt, StatusListTokenFormat.JWT, verificationTime)
            .getOrElse { error -> throw IllegalStateException("Invalid JWT signature", error) }
    }

    private fun parse(jwt: String): Pair<Header, StatusListTokenClaims> =
        jwtHeaderAndPayload<Header, StatusListTokenClaims>(jwt).getOrThrow()

    private fun Header.ensureTypeIsStatusListJwt() {
        checkNotNull(type) {
            "Missing `typ` from JOSE header"
        }
        check(type == TokenStatusListSpec.MEDIA_SUBTYPE_STATUS_LIST_JWT) {
            "Wrong `typ` expecting ${TokenStatusListSpec.MEDIA_SUBTYPE_STATUS_LIST_JWT} found $type"
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
