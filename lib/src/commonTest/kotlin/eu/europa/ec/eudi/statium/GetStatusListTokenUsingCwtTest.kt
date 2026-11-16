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

import eu.europa.ec.eudi.statium.cose.ParseCwt
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.time.Clock
import kotlin.time.Instant

class GetStatusListTokenUsingCwtTest {

    @Test
    fun cwtTaggedWith61IsRejectedEvenWithACustomParser() = runTest {
        var verifierCalled = false
        var parserCalled = false
        // 61(18([...])) - the content after the tags is irrelevant, it must be rejected upfront
        val taggedAsCwt = byteArrayOf(0xD8.toByte(), 0x3D, 0xD2.toByte(), 0x84.toByte())

        val getStatusListToken = GetStatusListToken.usingCwt(
            clock = Clock.fixed(Instant.parse("2025-01-01T12:00:00Z")),
            httpClient = httpClientReturning(taggedAsCwt),
            verifyStatusListTokenSignature = VerifyStatusListTokenCwtSignature { _, _ ->
                verifierCalled = true
                Result.success(Unit)
            },
            // A permissive parser that would accept anything
            parseCwt = ParseCwt { _ ->
                parserCalled = true
                error("Parser must not be invoked")
            },
        )

        val error = assertFailsWith<IllegalStateException> {
            getStatusListToken("https://example.com/statuslists/1", null).getOrThrow()
        }
        assertEquals("CBOR tag 61 (CWT) is not allowed on Status List Tokens", error.message)
        assertFalse(verifierCalled, "Signature verification must not be invoked")
        assertFalse(parserCalled, "Parser must not be invoked")
    }

    private fun httpClientReturning(body: ByteArray) = HttpClient(
        MockEngine {
            respond(
                content = body,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT),
            )
        },
    )
}
