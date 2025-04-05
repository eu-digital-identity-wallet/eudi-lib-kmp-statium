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

import eu.europa.ec.eudi.statium.BitsPerStatus
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BitsPerStatusSerializerTest {

    @Test
    fun testSerialization() {
        // Test that each BitsPerStatus enum value is serialized to its corresponding bits value
        assertEquals("1", Json.Default.encodeToString(BitsPerStatus.One))
        assertEquals("2", Json.Default.encodeToString(BitsPerStatus.Two))
        assertEquals("4", Json.Default.encodeToString(BitsPerStatus.Four))
        assertEquals("8", Json.Default.encodeToString(BitsPerStatus.Eight))
    }

    @Test
    fun testDeserialization() {
        // Test that each bits value is deserialized to the correct BitsPerStatus enum value
        assertEquals(BitsPerStatus.One, Json.Default.decodeFromString("1"))
        assertEquals(BitsPerStatus.Two, Json.Default.decodeFromString("2"))
        assertEquals(BitsPerStatus.Four, Json.Default.decodeFromString("4"))
        assertEquals(BitsPerStatus.Eight, Json.Default.decodeFromString("8"))
    }

    @Test
    fun testRoundTrip() {
        // Test round-trip serialization and deserialization
        val values = BitsPerStatus.entries.toTypedArray()
        for (value in values) {
            val json = Json.Default.encodeToString(value)
            val decoded = Json.Default.decodeFromString<BitsPerStatus>(json)
            assertEquals(value, decoded)
        }
    }

    @Test
    fun testInvalidDeserialization() {
        // Test that deserializing a number other than 1, 2, 4, or 8 raises a SerializationException
        val invalidValues = listOf("0", "3", "5", "6", "7", "9", "10")
        for (invalidValue in invalidValues) {
            val exception = assertFailsWith<SerializationException> {
                Json.Default.decodeFromString<BitsPerStatus>(invalidValue)
            }
            // Verify the exception has the expected message
            assertTrue(exception.message?.contains("Invalid bits value") == true)
        }
    }
}
