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
package eu.europa.ec.eudi.statium.jose

import eu.europa.ec.eudi.statium.misc.Base64UrlNoPadding
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JwtTest {

    @Serializable
    data class TestHeader(val alg: String, val typ: String? = null)

    @Serializable
    data class TestPayload(val sub: String, val iss: String)

    @Test
    fun testValidJwt() {
        // Create a valid JWT with header and payload as JsonObjects
        val header = buildJsonObject {
            put("alg", JsonPrimitive("HS256"))
            put("typ", JsonPrimitive("JWT"))
        }
        val payload = buildJsonObject {
            put("sub", JsonPrimitive("1234567890"))
            put("iss", JsonPrimitive("test-issuer"))
        }

        // Encode header and payload to base64url
        val headerBase64 = Base64UrlNoPadding.encode(header.toString().encodeToByteArray())
        val payloadBase64 = Base64UrlNoPadding.encode(payload.toString().encodeToByteArray())

        // Create JWT string with a dummy signature
        val jwt = "$headerBase64.$payloadBase64.dummy-signature"

        // Test parsing with JsonObject types
        val result = jwtHeaderAndPayload<JsonObject, JsonObject>(jwt)
        assertTrue(result.isSuccess)

        val (parsedHeader, parsedPayload) = result.getOrThrow()
        assertEquals("HS256", parsedHeader["alg"]?.toString()?.trim('"'))
        assertEquals("JWT", parsedHeader["typ"]?.toString()?.trim('"'))
        assertEquals("1234567890", parsedPayload["sub"]?.toString()?.trim('"'))
        assertEquals("test-issuer", parsedPayload["iss"]?.toString()?.trim('"'))

        // Test parsing with specific data classes
        val typedResult = jwtHeaderAndPayload<TestHeader, TestPayload>(jwt)
        assertTrue(typedResult.isSuccess)

        val (typedHeader, typedPayload) = typedResult.getOrThrow()
        assertEquals("HS256", typedHeader.alg)
        assertEquals("JWT", typedHeader.typ)
        assertEquals("1234567890", typedPayload.sub)
        assertEquals("test-issuer", typedPayload.iss)
    }

    @Test
    fun testJwtWithInvalidNumberOfParts() {
        // Test with JWT that doesn't have 3 parts
        val invalidJwt1 = "header.payload" // Only 2 parts
        val invalidJwt2 = "header.payload.signature.extra" // 4 parts

        // Test with 2 parts
        val result1 = jwtHeaderAndPayload<JsonObject, JsonObject>(invalidJwt1)
        assertTrue(result1.isFailure)
        assertFailsWith<IllegalArgumentException> {
            result1.getOrThrow()
        }

        // Test with 4 parts
        val result2 = jwtHeaderAndPayload<JsonObject, JsonObject>(invalidJwt2)
        assertTrue(result2.isFailure)
        assertFailsWith<IllegalArgumentException> {
            result2.getOrThrow()
        }
    }

    @Test
    fun testJwtWithInvalidBase64Encoding() {
        // Test with JWT that has 3 parts but first two aren't base64 encoded
        val invalidHeader = "not-base64-encoded"
        val invalidPayload = "also-not-base64"
        val invalidJwt = "$invalidHeader.$invalidPayload.signature"

        val result = jwtHeaderAndPayload<JsonObject, JsonObject>(invalidJwt)
        assertTrue(result.isFailure)
        // The failure should be due to base64 decoding error
        assertFailsWith<IllegalArgumentException> {
            result.getOrThrow()
        }
    }

    @Test
    fun testJwtWithInvalidJson() {
        // Test with JWT that has 3 parts, first two base64 encoded but not valid JSON
        val invalidJsonHeader = Base64UrlNoPadding.encode("not-a-json".encodeToByteArray())
        val invalidJsonPayload = Base64UrlNoPadding.encode("also-not-a-json".encodeToByteArray())
        val invalidJwt = "$invalidJsonHeader.$invalidJsonPayload.signature"

        val result = jwtHeaderAndPayload<JsonObject, JsonObject>(invalidJwt)
        assertTrue(result.isFailure)
        // The failure should be due to JSON parsing error
        assertFailsWith<Exception> {
            result.getOrThrow()
        }
    }
}
