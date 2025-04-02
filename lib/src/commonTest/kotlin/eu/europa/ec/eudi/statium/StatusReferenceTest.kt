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

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StatusReferenceTest {

    @Test
    fun testCreation() {
        val index = StatusIndex(42)
        val uri = "https://example.com/status/123"
        val statusReference = StatusReference(index, uri)

        assertEquals(index, statusReference.index)
        assertEquals(uri, statusReference.uri)
    }

    @Test
    fun testCreationWithEmptyUriFails() {
        val index = StatusIndex(42)
        val uri = ""

        assertFailsWith<IllegalArgumentException> {
            StatusReference(index, uri)
        }
    }

    @Test
    fun testCreationWithBlankUriFails() {
        val index = StatusIndex(42)
        val uri = "   "

        assertFailsWith<IllegalArgumentException> {
            StatusReference(index, uri)
        }
    }

    @Test
    fun testSerialization() {
        val index = StatusIndex(42)
        val uri = "https://example.com/status/123"
        val statusReference = StatusReference(index, uri)

        val json = Json.encodeToString(StatusReference.serializer(), statusReference)

        // Verify the JSON contains the expected field names and values
        val expectedJson = """{"idx":42,"uri":"https://example.com/status/123"}"""
        assertEquals(expectedJson, json)
    }

    @Test
    fun testDeserialization() {
        val json = """{"idx":42,"uri":"https://example.com/status/123"}"""

        val statusReference = Json.decodeFromString(StatusReference.serializer(), json)

        assertEquals(StatusIndex(42), statusReference.index)
        assertEquals("https://example.com/status/123", statusReference.uri)
    }

    @Test
    fun testRoundTrip() {
        val original = StatusReference(StatusIndex(42), "https://example.com/status/123")

        val json = Json.encodeToString(StatusReference.serializer(), original)
        val deserialized = Json.decodeFromString(StatusReference.serializer(), json)

        assertEquals(original, deserialized)
    }

    @Test
    fun testDeserializationWithMissingFieldsFails() {
        val jsonWithoutIndex = """{"uri":"https://example.com/status/123"}"""
        val jsonWithoutUri = """{"idx":42}"""

        assertFailsWith<kotlinx.serialization.MissingFieldException> {
            Json.decodeFromString(StatusReference.serializer(), jsonWithoutIndex)
        }

        assertFailsWith<kotlinx.serialization.MissingFieldException> {
            Json.decodeFromString(StatusReference.serializer(), jsonWithoutUri)
        }
    }
}
