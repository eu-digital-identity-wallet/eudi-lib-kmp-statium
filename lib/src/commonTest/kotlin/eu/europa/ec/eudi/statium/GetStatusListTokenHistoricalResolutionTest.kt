/*
 * Copyright (c) 2025-2026 European Commission
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

import eu.europa.ec.eudi.statium.misc.Base64UrlNoPadding
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class GetStatusListTokenHistoricalResolutionTest {

    private val uri = "https://example.com/statuslists/1"
    private val iat = Instant.parse("2025-01-01T12:00:00Z")
    private val exp = Instant.parse("2025-01-02T12:00:00Z")

    @Test
    fun tokenCoveringTheRequestedTimeIsAccepted() = runTest {
        val claims = getStatusListToken(unsignedJwt(iat, exp)).invoke(uri, iat + 1.minutes).getOrThrow()
        assertEquals(iat, claims.issuedAt)
    }

    @Test
    fun tokenIssuedAfterTheRequestedTimeIsRejectedEvenWithinClockSkew() = runTest {
        // A static server / CDN ignored `time` and returned the latest Status List Token.
        // The requested time is within the allowed clock skew, so only the `iat <= time` check catches it.
        val requestedTime = iat - 5.minutes
        val error = assertFailsWith<IllegalStateException> {
            getStatusListToken(unsignedJwt(iat, exp)).invoke(uri, requestedTime).getOrThrow()
        }
        assertTrue(error.message!!.contains("requested time"), error.message)
    }

    @Test
    fun tokenExpiredBeforeTheRequestedTimeIsRejectedEvenWithinClockSkew() = runTest {
        val requestedTime = exp + 5.minutes
        val error = assertFailsWith<IllegalStateException> {
            getStatusListToken(unsignedJwt(iat, exp)).invoke(uri, requestedTime).getOrThrow()
        }
        assertTrue(error.message!!.contains("requested time"), error.message)
    }

    private fun getStatusListToken(jwt: String): GetStatusListToken {
        val httpClient = HttpClient(
            MockEngine {
                respond(
                    content = jwt,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT),
                )
            },
        )
        return GetStatusListToken.usingJwt(
            clock = Clock.fixed(exp + 1.minutes),
            httpClient = httpClient,
            verifyStatusListTokenSignature = VerifyStatusListTokenJwtSignature.Ignore,
            allowedClockSkew = 10.minutes,
        )
    }

    private fun unsignedJwt(iat: Instant, exp: Instant): String {
        val header = """{"alg":"none","typ":"${TokenStatusListSpec.MEDIA_SUBTYPE_STATUS_LIST_JWT}"}"""
        val payload =
            """{"sub":"$uri","iat":${iat.epochSeconds},"exp":${exp.epochSeconds},"status_list":{"bits":1,"lst":"eNrbuRgAAhcBXQ"}}"""
        fun String.b64() = Base64UrlNoPadding.encode(encodeToByteArray())
        return "${header.b64()}.${payload.b64()}.c2ln"
    }
}
