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

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

class StatusListTokenClaimsTest {

    @Test
    fun testCreation() {
        val subject = "https://example.com/issuer"
        val issuedAt = Instant.fromEpochSeconds(1625097600L) // 2021-07-01T00:00:00Z
        val expirationTime = Instant.fromEpochSeconds(1656633600L) // 2022-07-01T00:00:00Z
        val timeToLive = TimeToLive(30.days)
        val statusList = TestVectors.TV1.statusList

        val claims = StatusListTokenClaims(
            subject = subject,
            issuedAt = issuedAt,
            expirationTime = expirationTime,
            timeToLive = timeToLive,
            statusList = statusList,
        )

        assertEquals(subject, claims.subject)
        assertEquals(issuedAt, claims.issuedAt)
        assertEquals(expirationTime, claims.expirationTime)
        assertEquals(timeToLive, claims.timeToLive)
        assertEquals(statusList, claims.statusList)
    }

    @Test
    fun testCreationWithEmptySubjectFails() {
        val issuedAt = Instant.fromEpochSeconds(1625097600L)
        val statusList = TestVectors.TV1.statusList

        assertFailsWith<IllegalArgumentException> {
            StatusListTokenClaims(
                subject = "",
                issuedAt = issuedAt,
                statusList = statusList,
            )
        }
    }

    @Test
    fun testCreationWithBlankSubjectFails() {
        val issuedAt = Instant.fromEpochSeconds(1625097600L)
        val statusList = TestVectors.TV1.statusList

        assertFailsWith<IllegalArgumentException> {
            StatusListTokenClaims(
                subject = "   ",
                issuedAt = issuedAt,
                statusList = statusList,
            )
        }
    }

    @Test
    fun testSerialization() {
        val subject = "https://example.com/issuer"
        val issuedAt = Instant.fromEpochSeconds(1625097600L) // 2021-07-01T00:00:00Z
        val expirationTime = Instant.fromEpochSeconds(1656633600L) // 2022-07-01T00:00:00Z
        val timeToLive = TimeToLive(30.days)
        val statusList = TestVectors.TV1.statusList

        val claims = StatusListTokenClaims(
            subject = subject,
            issuedAt = issuedAt,
            expirationTime = expirationTime,
            timeToLive = timeToLive,
            statusList = statusList,
        )

        val json = Json.encodeToString(StatusListTokenClaims.serializer(), claims)

        // Verify the JSON contains the expected field names and values
        val expectedJson = """{"sub":"https://example.com/issuer","iat":1625097600,"exp":1656633600,"ttl":2592000,"status_list":{"bits":1,"lst":"eNrt3AENwCAMAEGogklACtKQPg9LugC9k_ACvreiogEAAKkeCQAAAAAAAAAAAAAAAAAAAIBylgQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAXG9IAAAAAAAAAPwsJAAAAAAAAAAAAAAAvhsSAAAAAAAAAAAA7KpLAAAAAAAAAAAAAAAAAAAAAJsLCQAAAAAAAAAAADjelAAAAAAAAAAAKjDMAQAAAACAZC8L2AEb"}}"""
        assertEquals(expectedJson, json)
    }

    @Test
    fun testDeserialization() {
        val json = """{"sub":"https://example.com/issuer","iat":1625097600,"exp":1656633600,"ttl":2592000,"status_list":{"bits":1,"lst":"eNrt3AENwCAMAEGogklACtKQPg9LugC9k_ACvreiogEAAKkeCQAAAAAAAAAAAAAAAAAAAIBylgQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAXG9IAAAAAAAAAPwsJAAAAAAAAAAAAAAAvhsSAAAAAAAAAAAA7KpLAAAAAAAAAAAAAAAAAAAAAJsLCQAAAAAAAAAAADjelAAAAAAAAAAAKjDMAQAAAACAZC8L2AEb"}}"""

        val claims = Json.decodeFromString(StatusListTokenClaims.serializer(), json)

        assertEquals("https://example.com/issuer", claims.subject)
        assertEquals(Instant.fromEpochSeconds(1625097600L), claims.issuedAt)
        assertEquals(Instant.fromEpochSeconds(1656633600L), claims.expirationTime)
        assertEquals(TimeToLive(2592000.seconds), claims.timeToLive)
        assertEquals(TestVectors.TV1.statusList, claims.statusList)
    }

    @Test
    fun testRoundTrip() {
        val subject = "https://example.com/issuer"
        val issuedAt = Instant.fromEpochSeconds(1625097600L)
        val expirationTime = Instant.fromEpochSeconds(1656633600L)
        val timeToLive = TimeToLive(30.days)
        val statusList = TestVectors.TV1.statusList

        val original = StatusListTokenClaims(
            subject = subject,
            issuedAt = issuedAt,
            expirationTime = expirationTime,
            timeToLive = timeToLive,
            statusList = statusList,
        )

        val json = Json.encodeToString(StatusListTokenClaims.serializer(), original)
        val deserialized = Json.decodeFromString(StatusListTokenClaims.serializer(), json)

        assertEquals(original, deserialized)
    }

    @Test
    fun testDeserializationWithMissingRequiredFieldsFails() {
        val jsonWithoutSubject = """{"iat":1625097600,"status_list":{"bits":1,"lst":"eNrt3AENwCAMAEGogklACtKQPg9LugC9k_ACvreiogEAAKkeCQAAAAAAAAAAAAAAAAAAAIBylgQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAXG9IAAAAAAAAAPwsJAAAAAAAAAAAAAAAvhsSAAAAAAAAAAAA7KpLAAAAAAAAAAAAAAAAAAAAAJsLCQAAAAAAAAAAADjelAAAAAAAAAAAKjDMAQAAAACAZC8L2AEb"}}"""
        val jsonWithoutIssuedAt = """{"sub":"https://example.com/issuer","status_list":{"bits":1,"lst":"eNrt3AENwCAMAEGogklACtKQPg9LugC9k_ACvreiogEAAKkeCQAAAAAAAAAAAAAAAAAAAIBylgQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAXG9IAAAAAAAAAPwsJAAAAAAAAAAAAAAAvhsSAAAAAAAAAAAA7KpLAAAAAAAAAAAAAAAAAAAAAJsLCQAAAAAAAAAAADjelAAAAAAAAAAAKjDMAQAAAACAZC8L2AEb"}}"""
        val jsonWithoutStatusList = """{"sub":"https://example.com/issuer","iat":1625097600,"exp":1656633600,"ttl":2592000}"""

        assertFailsWith<kotlinx.serialization.MissingFieldException> {
            Json.decodeFromString(StatusListTokenClaims.serializer(), jsonWithoutSubject)
        }

        assertFailsWith<kotlinx.serialization.MissingFieldException> {
            Json.decodeFromString(StatusListTokenClaims.serializer(), jsonWithoutIssuedAt)
        }

        assertFailsWith<kotlinx.serialization.MissingFieldException> {
            Json.decodeFromString(StatusListTokenClaims.serializer(), jsonWithoutStatusList)
        }
    }

    @Test
    fun testDeserializationWithDifferentStatusListBits() {
        // Test with TV2 (2 bits per status)
        val jsonWithTV2 = """{"sub":"https://example.com/issuer","iat":1625097600,"status_list":{"bits":2,"lst":"eNrt2zENACEQAEEuoaBABP5VIO01fCjIHTMStt9ovGVIAAAAAABAbiEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEB5WwIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAID0ugQAAAAAAAAAAAAAAAAAQG12SgAAAAAAAAAAAAAAAAAAAAAAAAAAAOCSIQEAAAAAAAAAAAAAAAAAAAAAAAD8ExIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwJEuAQAAAAAAAAAAAAAAAAAAAAAAAMB9SwIAAAAAAAAAAAAAAAAAAACoYUoAAAAAAAAAAAAAAEBqH81gAQw"}}"""

        val claims = Json.decodeFromString(StatusListTokenClaims.serializer(), jsonWithTV2)

        assertEquals("https://example.com/issuer", claims.subject)
        assertEquals(Instant.fromEpochSeconds(1625097600L), claims.issuedAt)
        assertEquals(TestVectors.TV2.statusList, claims.statusList)
    }

    @Test
    fun testDeserializationWithOptionalFieldsOmitted() {
        val jsonWithoutOptionalFields = """{"sub":"https://example.com/issuer","iat":1625097600,"status_list":{"bits":1,"lst":"eNrt3AENwCAMAEGogklACtKQPg9LugC9k_ACvreiogEAAKkeCQAAAAAAAAAAAAAAAAAAAIBylgQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAXG9IAAAAAAAAAPwsJAAAAAAAAAAAAAAAvhsSAAAAAAAAAAAA7KpLAAAAAAAAAAAAAAAAAAAAAJsLCQAAAAAAAAAAADjelAAAAAAAAAAAKjDMAQAAAACAZC8L2AEb"}}"""

        val claims = Json.decodeFromString(StatusListTokenClaims.serializer(), jsonWithoutOptionalFields)

        assertEquals("https://example.com/issuer", claims.subject)
        assertEquals(Instant.fromEpochSeconds(1625097600L), claims.issuedAt)
        assertNull(claims.expirationTime)
        assertNull(claims.timeToLive)
        assertEquals(TestVectors.TV1.statusList, claims.statusList)
    }
}
