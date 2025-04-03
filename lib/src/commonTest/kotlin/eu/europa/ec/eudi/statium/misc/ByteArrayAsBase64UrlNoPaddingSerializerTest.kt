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
package eu.europa.ec.eudi.statium.misc

import eu.europa.ec.eudi.statium.CompressedByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ByteArrayAsBase64UrlNoPaddingSerializerTest {

    private val json = JsonIgnoringUnknownKeys

    @Test
    fun testSerializeEmptyByteArray() {
        val byteArray = ByteArray(0)
        val serialized = json.encodeToString(ByteArrayAsBase64UrlNoPaddingSerializer, byteArray)
        assertEquals("\"\"", serialized, "Serializing an empty byte array should result in an empty string")
    }

    @Test
    fun testDeserializeEmptyString() {
        val serialized = "\"\""
        val deserialized = json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, serialized)
        assertContentEquals(ByteArray(0), deserialized, "Deserializing an empty string should result in an empty byte array")
    }

    @Test
    fun testSerializeSimpleByteArray() {
        val byteArray = "Hello, World!".encodeToByteArray()
        val serialized = json.encodeToString(ByteArrayAsBase64UrlNoPaddingSerializer, byteArray)
        assertEquals("\"SGVsbG8sIFdvcmxkIQ\"", serialized, "Serializing 'Hello, World!' bytes should result in 'SGVsbG8sIFdvcmxkIQ'")
    }

    @Test
    fun testDeserializeSimpleString() {
        val serialized = "\"SGVsbG8sIFdvcmxkIQ\""
        val deserialized = json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, serialized)
        val expected = "Hello, World!".encodeToByteArray()
        assertContentEquals(expected, deserialized, "Deserializing 'SGVsbG8sIFdvcmxkIQ' should result in 'Hello, World!' bytes")
    }

    @Test
    fun testSerializeWithSpecialCharacters() {
        val byteArray = "Special characters: !@#$%^&*()_+".encodeToByteArray()
        val serialized = json.encodeToString(ByteArrayAsBase64UrlNoPaddingSerializer, byteArray)
        val deserialized = json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, serialized)
        assertContentEquals(byteArray, deserialized, "Serializing and then deserializing should result in the original byte array")
    }

    @Test
    fun testSerializeWithNonAsciiCharacters() {
        val byteArray = "Non-ASCII characters: äöüßÄÖÜ€".encodeToByteArray()
        val serialized = json.encodeToString(ByteArrayAsBase64UrlNoPaddingSerializer, byteArray)
        val deserialized = json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, serialized)
        assertContentEquals(byteArray, deserialized, "Serializing and then deserializing should result in the original byte array")
    }

    @Test
    fun testSerializeBinaryData() {
        val byteArray = ByteArray(256) { it.toByte() }
        val serialized = json.encodeToString(ByteArrayAsBase64UrlNoPaddingSerializer, byteArray)
        val deserialized = json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, serialized)
        assertContentEquals(byteArray, deserialized, "Serializing and then deserializing binary data should result in the original byte array")
    }

    @Test
    fun testDeserializeInvalidBase64() {
        val invalidBase64 = "\"This is not valid Base64!\""
        assertFailsWith<SerializationException> {
            json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, invalidBase64)
        }
    }

    @Test
    fun testRoundTrip() {
        val testCases = listOf(
            ByteArray(0),
            "Simple string".encodeToByteArray(),
            "String with spaces and punctuation!".encodeToByteArray(),
            "1234567890".encodeToByteArray(),
            "Special characters: !@#$%^&*()_+".encodeToByteArray(),
            "Non-ASCII characters: äöüßÄÖÜ€".encodeToByteArray(),
            ByteArray(100) { it.toByte() },
        )

        for (testCase in testCases) {
            val serialized = json.encodeToString(ByteArrayAsBase64UrlNoPaddingSerializer, testCase)
            val deserialized = json.decodeFromString(ByteArrayAsBase64UrlNoPaddingSerializer, serialized)
            assertContentEquals(testCase, deserialized, "Round trip serialization and deserialization should result in the original byte array")
        }
    }

    @Test
    fun testTypealias() {
        // Test that the ByteArrayAsBase64UrlNoPadding typealias works correctly
        @Serializable
        class TestData(val data: CompressedByteArray)

        val original = TestData("Hello, World!".encodeToByteArray())
        val serialized = json.encodeToString(serializer<TestData>(), original)

        // The serialized string should contain the Base64URL-encoded bytes
        val expected = "{\"data\":\"SGVsbG8sIFdvcmxkIQ\"}"
        assertEquals(expected, serialized, "The typealias should serialize correctly")

        // Deserialize and verify
        val deserialized = json.decodeFromString<TestData>(serialized)
        assertContentEquals(original.data, deserialized.data, "The typealias should deserialize correctly")
    }
}
