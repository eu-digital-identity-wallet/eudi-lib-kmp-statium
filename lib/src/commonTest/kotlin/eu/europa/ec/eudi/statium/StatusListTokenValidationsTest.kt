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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class StatusListTokenValidationsTest : StatusListTokenValidations {

    // Sample StatusList for testing
    private val sampleStatusList = StatusList.fromBase64UrlEncodedList(
        bytesPerStatus = BitsPerStatus.One,
        base64UrlEncodedList = "AAAA",
    ).getOrThrow()

    // Test data
    private val subject = "test-subject"
    private val now = Instant.parse("2023-01-01T12:00:00Z")
    private val past = Instant.parse("2022-01-01T12:00:00Z")
    private val future = Instant.parse("2024-01-01T12:00:00Z")

    @Test
    fun testEnsureSubjectSuccess() {
        val claims = createClaims(subject = subject)
        val result = claims.ensureSubject(subject)
        assertEquals(claims, result)
    }

    @Test
    fun testEnsureSubjectFailure() {
        val claims = createClaims(subject = "wrong-subject")
        assertFailsWith<IllegalStateException> {
            claims.ensureSubject(subject)
        }
    }

    @Test
    fun testEnsureIssuedBeforeSuccess() {
        // Create claims issued in the past
        val claims = createClaims(issuedAt = past)

        val result = claims.ensureIssuedBefore(now, Duration.ZERO)
        assertEquals(claims, result)
    }

    @Test
    fun testEnsureIssuedBeforeFailure() {
        // Create claims issued in the future
        val claims = createClaims(issuedAt = future)

        assertFailsWith<IllegalStateException> {
            claims.ensureIssuedBefore(now, Duration.ZERO)
        }
    }

    @Test
    fun testEnsureIssuedBeforeWithClockSkewSuccess() {
        // Create claims issued slightly in the future
        val slightlyFuture = now.plus(5.minutes) // 5 minutes in the future
        val claims = createClaims(issuedAt = slightlyFuture)

        // With a clock skew of 10 minutes, this should pass
        val clockSkew = 10.minutes
        val result = claims.ensureIssuedBefore(now, clockSkew)
        assertEquals(claims, result)
    }

    @Test
    fun testEnsureIssuedBeforeWithClockSkewFailure() {
        // Create claims issued far in the future
        val farFuture = now.plus(20.minutes) // 20 minutes in the future
        val claims = createClaims(issuedAt = farFuture)

        // With a clock skew of 10 minutes, this should fail
        val clockSkew = 10.minutes
        assertFailsWith<IllegalStateException> {
            claims.ensureIssuedBefore(now, clockSkew)
        }
    }

    @Test
    fun testEnsureNotExpiredSuccess() {
        // Create claims that expire in the future
        val claims = createClaims(expirationTime = future)

        val result = claims.ensureNotExpired(now, Duration.ZERO)
        assertEquals(claims, result)
    }

    @Test
    fun testEnsureNotExpiredFailure() {
        // Create claims that expired in the past
        val claims = createClaims(expirationTime = past)

        // Validation at current time should fail
        assertFailsWith<IllegalStateException> {
            claims.ensureNotExpired(now, Duration.ZERO)
        }
    }

    @Test
    fun testEnsureNotExpiredWithNullExpiration() {
        // Create claims with no expiration time
        val claims = createClaims(expirationTime = null)
        val result = claims.ensureNotExpired(now, Duration.ZERO)

        assertEquals(claims, result)
    }

    @Test
    fun testEnsureNotExpiredWithClockSkewSuccess() {
        // Create claims that expired slightly in the past
        val slightlyPast = now.minus(5.minutes) // 5 minutes in the past
        val claims = createClaims(expirationTime = slightlyPast)

        // With a clock skew of 10 minutes, this should pass
        val clockSkew = 10.minutes
        val result = claims.ensureNotExpired(now, clockSkew)
        assertEquals(claims, result)
    }

    @Test
    fun testEnsureNotExpiredWithClockSkewFailure() {
        // Create claims that expired far in the past
        val farPast = now.minus(20.minutes) // 20 minutes in the past
        val claims = createClaims(expirationTime = farPast)

        // With a clock skew of 10 minutes, this should fail
        val clockSkew = 10.minutes
        assertFailsWith<IllegalStateException> {
            claims.ensureNotExpired(now, clockSkew)
        }
    }

    @Test
    fun testEnsureValidSuccess() {
        // Create valid claims (correct subject, issued in past, expires in future)
        val claims = createClaims(
            subject = subject,
            issuedAt = past,
            expirationTime = future,
        )

        // Full validation should succeed
        val result = claims.ensureValid(subject, now, Duration.ZERO)

        // Result should be the same claims object
        assertEquals(claims, result)
    }

    @Test
    fun testEnsureValidFailsWithWrongSubject() {
        // Create claims with wrong subject
        val claims = createClaims(
            subject = "wrong-subject",
            issuedAt = past,
            expirationTime = future,
        )

        // Validation should fail due to subject mismatch
        assertFailsWith<IllegalStateException> {
            claims.ensureValid(subject, now, Duration.ZERO)
        }
    }

    @Test
    fun testEnsureValidFailsWithFutureIssuedAt() {
        // Create claims issued in the future
        val claims = createClaims(
            subject = subject,
            issuedAt = future,
            expirationTime = future,
        )

        // Validation should fail due to future issuance
        assertFailsWith<IllegalStateException> {
            claims.ensureValid(subject, now, Duration.ZERO)
        }
    }

    @Test
    fun testEnsureValidFailsWithPastExpiration() {
        // Create claims that expired in the past
        val claims = createClaims(
            subject = subject,
            issuedAt = past,
            expirationTime = past,
        )

        // Validation should fail due to expiration
        assertFailsWith<IllegalStateException> {
            claims.ensureValid(subject, now, Duration.ZERO)
        }
    }

    @Test
    fun testEnsureValidWithClockSkewSuccess() {
        // Create claims with slightly future issuedAt and slightly past expiration
        val slightlyFuture = now.plus(5.minutes) // 5 minutes in the future
        val slightlyPast = now.minus(5.minutes) // 5 minutes in the past
        val claims = createClaims(
            subject = subject,
            issuedAt = slightlyFuture,
            expirationTime = slightlyPast,
        )

        // With a clock skew of 10 minutes, both checks should pass
        val clockSkew = 10.minutes
        val result = claims.ensureValid(subject, now, clockSkew)
        assertEquals(claims, result)
    }

    // Helper method to create StatusListTokenClaims for testing
    private fun createClaims(
        subject: String = this.subject,
        issuedAt: Instant = now,
        expirationTime: Instant? = null,
        timeToLive: PositiveDurationAsSeconds? = null,
    ): StatusListTokenClaims =
        StatusListTokenClaims(
            subject = subject,
            issuedAt = issuedAt,
            expirationTime = expirationTime,
            timeToLive = timeToLive,
            statusList = sampleStatusList,
        )
}
