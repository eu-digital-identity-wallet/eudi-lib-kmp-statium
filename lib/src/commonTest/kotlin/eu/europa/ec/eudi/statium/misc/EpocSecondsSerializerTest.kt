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

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

class EpocSecondsSerializerTest {

    private val json = Json

    @Test
    fun testSerializeEpochStart() {
        val instant = Instant.fromEpochSeconds(0)
        val serialized = json.encodeToString(EpocSecondsSerializer, instant)
        assertEquals("0", serialized, "Serializing epoch start should result in '0'")
    }

    @Test
    fun testDeserializeEpochStart() {
        val serialized = "0"
        val deserialized = json.decodeFromString(EpocSecondsSerializer, serialized)
        assertEquals(Instant.fromEpochSeconds(0), deserialized, "Deserializing '0' should result in epoch start")
    }

    @Test
    fun testSerializePositiveSeconds() {
        val instant = Instant.fromEpochSeconds(1609459200) // 2021-01-01T00:00:00Z
        val serialized = json.encodeToString(EpocSecondsSerializer, instant)
        assertEquals("1609459200", serialized, "Serializing 2021-01-01T00:00:00Z should result in '1609459200'")
    }

    @Test
    fun testDeserializePositiveSeconds() {
        val serialized = "1609459200"
        val deserialized = json.decodeFromString(EpocSecondsSerializer, serialized)
        assertEquals(Instant.fromEpochSeconds(1609459200), deserialized, "Deserializing '1609459200' should result in 2021-01-01T00:00:00Z")
    }

    @Test
    fun testSerializeNegativeSeconds() {
        val instant = Instant.fromEpochSeconds(-1609459200) // 1919-01-01T00:00:00Z (approximately)
        val serialized = json.encodeToString(EpocSecondsSerializer, instant)
        assertEquals("-1609459200", serialized, "Serializing a date before epoch should result in a negative number")
    }

    @Test
    fun testDeserializeNegativeSeconds() {
        val serialized = "-1609459200"
        val deserialized = json.decodeFromString(EpocSecondsSerializer, serialized)
        assertEquals(Instant.fromEpochSeconds(-1609459200), deserialized, "Deserializing '-1609459200' should result in a date before epoch")
    }

    @Test
    fun testSerializeCurrentTime() {
        val now = Instant.fromEpochSeconds(Clock.System.now().epochSeconds)
        val serialized = json.encodeToString(EpocSecondsSerializer, now)
        assertEquals(now.epochSeconds.toString(), serialized, "Serializing current time should result in the current epoch seconds")
    }

    @Test
    fun testSerializeFarFutureDate() {
        val farFuture = Instant.fromEpochSeconds(32503680000) // Year 3000 (approximately)
        val serialized = json.encodeToString(EpocSecondsSerializer, farFuture)
        assertEquals("32503680000", serialized, "Serializing a far future date should result in a large positive number")
    }

    @Test
    fun testDeserializeFarFutureDate() {
        val serialized = "32503680000"
        val deserialized = json.decodeFromString(EpocSecondsSerializer, serialized)
        assertEquals(Instant.fromEpochSeconds(32503680000), deserialized, "Deserializing '32503680000' should result in a far future date")
    }

    @Test
    fun testRoundTrip() {
        val testCases = listOf(
            Instant.fromEpochSeconds(0), // Epoch start
            Instant.fromEpochSeconds(1), // 1 second after epoch
            Instant.fromEpochSeconds(-1), // 1 second before epoch
            Instant.fromEpochSeconds(1609459200), // 2021-01-01T00:00:00Z
            Instant.fromEpochSeconds(-1609459200), // 1919-01-01T00:00:00Z (approximately)
            Instant.fromEpochSeconds(Clock.System.now().epochSeconds), // Current time
            Instant.fromEpochSeconds(32503680000), // Year 3000 (approximately)
        )

        for (testCase in testCases) {
            val serialized = json.encodeToString(EpocSecondsSerializer, testCase)
            val deserialized = json.decodeFromString(EpocSecondsSerializer, serialized)
            assertEquals(testCase, deserialized, "Round trip serialization and deserialization should result in the original instant")
        }
    }
}
