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
package eu.europa.ec.eudi.statium.http

import eu.europa.ec.eudi.statium.StatusListTokenFormat
import eu.europa.ec.eudi.statium.TokenStatusListSpec
import eu.europa.ec.eudi.statium.http.GetStatusListTokenKtorOps.Companion.contentType
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class GetStatusListTokenKtorOpsTest : GetStatusListTokenKtorOps {

    @Test
    fun testWithMockEngine() = runTest {
        // Create a mock engine
        val mockEngine = MockEngine { request ->
            // Verify the request method and headers
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(
                TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT,
                request.headers[HttpHeaders.Accept],
            )

            // Verify no query parameters are present
            assertTrue(request.url.parameters.isEmpty())

            // Return a mock response
            respond(
                content = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT),
            )
        }

        // Create HttpClient with the mock engine
        val client = HttpClient(mockEngine)

        // Call the method under test
        val result = client.getStatusListToken(
            "https://example.com/status",
            StatusListTokenFormat.JWT,
            null,
        )

        // Verify the result
        assertTrue(result.isSuccess)
        assertEquals(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
            result.getOrThrow(),
        )
    }

    @Test
    fun testWithMockEngineAndTimestamp() = runTest {
        val testTime = Instant.fromEpochSeconds(1234567890)

        // Create a mock engine
        val mockEngine = MockEngine { request ->
            // Verify the request method and headers
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(
                TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT,
                request.headers[HttpHeaders.Accept],
            )

            // Verify the time query parameter is present and has the correct value
            assertEquals("1234567890", request.url.parameters[TokenStatusListSpec.TIME])

            // Return a mock response
            respond(
                content = "d2845824a3012603686b63726564656e7469616c5374617475738143a101a1054c0102030405060708090a0b0c0d0e0f",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT),
            )
        }

        // Create HttpClient with the mock engine
        val client = HttpClient(mockEngine)

        // Call the method under test
        val result = client.getStatusListToken(
            "https://example.com/status",
            StatusListTokenFormat.CWT,
            testTime,
        )

        // Verify the result
        assertTrue(result.isSuccess)
        assertEquals(
            "d2845824a3012603686b63726564656e7469616c5374617475738143a101a1054c0102030405060708090a0b0c0d0e0f",
            result.getOrThrow(),
        )
    }

    @Test
    fun testContentTypeForJWT() {
        val contentType = StatusListTokenFormat.JWT.contentType()

        // Test that the content type has the correct value
        assertEquals("application/statuslist+jwt", contentType.toString())

        // Test the individual components of the content type
        assertEquals("application", contentType.contentType)
        assertEquals("statuslist+jwt", contentType.contentSubtype)
    }

    @Test
    fun testContentTypeForCWT() {
        val contentType = StatusListTokenFormat.CWT.contentType()

        // Test that the content type has the correct value
        assertEquals("application/statuslist+cwt", contentType.toString())

        // Test the individual components of the content type
        assertEquals("application", contentType.contentType)
        assertEquals("statuslist+cwt", contentType.contentSubtype)
    }

    @Test
    fun testContentTypeMatchesSpecConstants() {
        // Test that the content types match the constants defined in TokenStatusListSpec
        val jwtContentType = StatusListTokenFormat.JWT.contentType()
        assertEquals(TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT, jwtContentType.toString())

        val cwtContentType = StatusListTokenFormat.CWT.contentType()
        assertEquals(TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT, cwtContentType.toString())
    }

    @Test
    fun testWithMockEngineReturning500Error() = runTest {
        // Create a mock engine that returns a 500 error
        val mockEngine = MockEngine { request ->
            // Verify the request method and headers
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(
                TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT,
                request.headers[HttpHeaders.Accept],
            )

            // Return a 500 error response
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
            )
        }

        // Create HttpClient with the mock engine
        val client = HttpClient(mockEngine)

        // Call the method under test
        val result = client.getStatusListToken(
            "https://example.com/status",
            StatusListTokenFormat.JWT,
            null,
        )

        // Verify the exception is thrown with the expected message
        val exception = assertFailsWith<IllegalStateException> {
            result.getOrThrow()
        }
        assertEquals("Got status 500 Internal Server Error while calling https://example.com/status", exception.message)
    }

    @Test
    fun testWithMockEngineReturning4xxError() = runTest {
        // Create a mock engine that returns a 404 error
        val mockEngine = MockEngine { request ->
            // Verify the request method and headers
            assertEquals(HttpMethod.Get, request.method)
            assertEquals(
                TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT,
                request.headers[HttpHeaders.Accept],
            )

            // Return a 404 error response
            respond(
                content = "Not Found",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Plain.toString()),
            )
        }

        // Create HttpClient with the mock engine
        val client = HttpClient(mockEngine)

        // Call the method under test
        val result = client.getStatusListToken(
            "https://example.com/status",
            StatusListTokenFormat.CWT,
            null,
        )

        // Verify the exception is thrown with the expected message
        val exception = assertFailsWith<IllegalStateException> {
            result.getOrThrow()
        }
        assertEquals("Got status 404 Not Found while calling https://example.com/status", exception.message)
    }
}
