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
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationAsSecondsSerializerTest {

    private val json = Json

    @Test
    fun testSerializeZeroSeconds() {
        val duration = 0.seconds
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("0", serialized, "Serializing 0 seconds should result in '0'")
    }

    @Test
    fun testDeserializeZeroSeconds() {
        val serialized = "0"
        val deserialized = json.decodeFromString(DurationAsSecondsSerializer, serialized)
        assertEquals(0.seconds, deserialized, "Deserializing '0' should result in 0 seconds")
    }

    @Test
    fun testSerializePositiveSeconds() {
        val duration = 60.seconds
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("60", serialized, "Serializing 60 seconds should result in '60'")
    }

    @Test
    fun testDeserializePositiveSeconds() {
        val serialized = "60"
        val deserialized = json.decodeFromString(DurationAsSecondsSerializer, serialized)
        assertEquals(60.seconds, deserialized, "Deserializing '60' should result in 60 seconds")
    }

    @Test
    fun testSerializeMinutes() {
        val duration = 5.minutes
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("300", serialized, "Serializing 5 minutes should result in '300'")
    }

    @Test
    fun testSerializeHours() {
        val duration = 2.hours
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("7200", serialized, "Serializing 2 hours should result in '7200'")
    }

    @Test
    fun testSerializeDays() {
        val duration = 1.days
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("86400", serialized, "Serializing 1 day should result in '86400'")
    }

    @Test
    fun testSerializeComplexDuration() {
        val duration = 1.days + 2.hours + 30.minutes + 15.seconds
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("95415", serialized, "Serializing 1d 2h 30m 15s should result in '95415'")
    }

    @Test
    fun testDeserializeComplexDuration() {
        val serialized = "95415"
        val deserialized = json.decodeFromString(DurationAsSecondsSerializer, serialized)
        assertEquals(95415.seconds, deserialized, "Deserializing '95415' should result in 95415 seconds")
    }

    @Test
    fun testSerializeLargeDuration() {
        val duration = 365.days
        val serialized = json.encodeToString(DurationAsSecondsSerializer, duration)
        assertEquals("31536000", serialized, "Serializing 365 days should result in '31536000'")
    }

    @Test
    fun testDeserializeLargeDuration() {
        val serialized = "31536000"
        val deserialized = json.decodeFromString(DurationAsSecondsSerializer, serialized)
        assertEquals(31536000.seconds, deserialized, "Deserializing '31536000' should result in 31536000 seconds")
    }

    @Test
    fun testRoundTrip() {
        val testCases = listOf(
            0.seconds,
            1.seconds,
            60.seconds,
            5.minutes,
            2.hours,
            1.days,
            1.days + 2.hours + 30.minutes + 15.seconds,
            365.days,
        )

        for (testCase in testCases) {
            val serialized = json.encodeToString(DurationAsSecondsSerializer, testCase)
            val deserialized = json.decodeFromString(DurationAsSecondsSerializer, serialized)
            assertEquals(testCase, deserialized, "Round trip serialization and deserialization should result in the original duration")
        }
    }
}
