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

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Base64UrlNoPaddingTest {

    @Test
    fun testEncodeEmptyByteArray() {
        val input = ByteArray(0)
        val encoded = Base64UrlNoPadding.encode(input)
        assertEquals("", encoded, "Encoding an empty byte array should result in an empty string")
    }

    @Test
    fun testDecodeEmptyString() {
        val input = ""
        val decoded = Base64UrlNoPadding.decode(input)
        assertContentEquals(ByteArray(0), decoded, "Decoding an empty string should result in an empty byte array")
    }

    @Test
    fun testEncodeSimpleString() {
        val input = "Hello, World!".encodeToByteArray()
        val encoded = Base64UrlNoPadding.encode(input)
        assertEquals("SGVsbG8sIFdvcmxkIQ", encoded, "Encoding 'Hello, World!' should result in 'SGVsbG8sIFdvcmxkIQ'")
    }

    @Test
    fun testDecodeSimpleString() {
        val input = "SGVsbG8sIFdvcmxkIQ"
        val decoded = Base64UrlNoPadding.decode(input)
        val expected = "Hello, World!".encodeToByteArray()
        assertContentEquals(expected, decoded, "Decoding 'SGVsbG8sIFdvcmxkIQ' should result in 'Hello, World!'")
    }

    @Test
    fun testEncodeWithSpecialCharacters() {
        val input = "Special characters: !@#$%^&*()_+".encodeToByteArray()
        val encoded = Base64UrlNoPadding.encode(input)
        val decoded = Base64UrlNoPadding.decode(encoded)
        assertContentEquals(input, decoded, "Encoding and then decoding should result in the original byte array")
    }

    @Test
    fun testEncodeWithNonAsciiCharacters() {
        val input = "Non-ASCII characters: äöüßÄÖÜ€".encodeToByteArray()
        val encoded = Base64UrlNoPadding.encode(input)
        val decoded = Base64UrlNoPadding.decode(encoded)
        assertContentEquals(input, decoded, "Encoding and then decoding should result in the original byte array")
    }

    @Test
    fun testEncodeWithPartialByteArray() {
        val input = "Hello, World!".encodeToByteArray()
        val encoded = Base64UrlNoPadding.encode(input, 0, 5) // Only encode "Hello"
        assertEquals("SGVsbG8", encoded, "Encoding 'Hello' should result in 'SGVsbG8'")
    }

    @Test
    fun testRoundTrip() {
        val testCases = listOf(
            "Simple string",
            "String with spaces and punctuation!",
            "1234567890",
            "Special characters: !@#$%^&*()_+",
            "Non-ASCII characters: äöüßÄÖÜ€",
            "A very long string that will be encoded and then decoded to ensure that the round trip works correctly for longer inputs as well.",
        )

        for (testCase in testCases) {
            val input = testCase.encodeToByteArray()
            val encoded = Base64UrlNoPadding.encode(input)
            val decoded = Base64UrlNoPadding.decode(encoded)
            val decodedString = decoded.decodeToString()
            assertEquals(testCase, decodedString, "Round trip encoding and decoding should result in the original string")
        }
    }
}
