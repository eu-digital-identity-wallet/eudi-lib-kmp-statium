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

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatusTest {

    /**
     * Generates a random byte that satisfies Status.isApplicationSpecific()
     * This will either return 0x03 or a value in the range 0x0B..0x0F
     */
    private fun generateRandomApplicationSpecificByte(): UByte {
        return if (Random.nextBoolean()) {
            // Return STATUS_APPLICATION_SPECIFIC (0x03)
            TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC
        } else {
            // Return a random value in the application specific range (0x0B..0x0F)
            val rangeStart = TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_START.toInt()
            val rangeEnd = TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_END.toInt()
            Random.nextInt(rangeStart, rangeEnd + 1).toUByte()
        }
    }

    @Test
    fun testInvokeValid() {
        val status = Status(TokenStatusListSpec.STATUS_VALID)
        assertEquals(Status.Valid, status)
    }

    @Test
    fun testInvokeInvalid() {
        val status = Status(TokenStatusListSpec.STATUS_INVALID)
        assertEquals(Status.Invalid, status)
    }

    @Test
    fun testInvokeSuspended() {
        val status = Status(TokenStatusListSpec.STATUS_SUSPENDED)
        assertEquals(Status.Suspended, status)
    }

    @Test
    fun testInvokeApplicationSpecific() {
        val status = Status(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC)
        assertEquals(Status.ApplicationSpecific(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC), status)
    }

    @Test
    fun testInvokeApplicationSpecificHex3() {
        val status = Status(0x3u)
        assertEquals(Status.ApplicationSpecific(0x3u), status)
    }

    @Test
    fun testInvokeApplicationSpecificRange() {
        val status = Status(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_START)
        assertEquals(Status.ApplicationSpecific(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_START), status)

        val statusEnd = Status(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_END)
        assertEquals(Status.ApplicationSpecific(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_END), statusEnd)
    }

    @Test
    fun testInvokeReserved() {
        val reservedValue: UByte = 0x04u
        val status = Status(reservedValue)
        assertEquals(Status.Reserved(reservedValue), status)
    }

    @Test
    fun testToByteValid() {
        assertEquals(TokenStatusListSpec.STATUS_VALID, Status.Valid.toUByte())
    }

    @Test
    fun testToByteInvalid() {
        assertEquals(TokenStatusListSpec.STATUS_INVALID, Status.Invalid.toUByte())
    }

    @Test
    fun testToByteSuspended() {
        assertEquals(TokenStatusListSpec.STATUS_SUSPENDED, Status.Suspended.toUByte())
    }

    @Test
    fun testToByteApplicationSpecific() {
        val value = TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC
        assertEquals(value, Status.ApplicationSpecific(value).toUByte())
    }

    @Test
    fun testToByteReserved() {
        val value: UByte = 0x04u
        assertEquals(value, Status.Reserved(value).toUByte())
    }

    @Test
    fun testIsApplicationSpecificHappyPath() {
        // Test exact match with STATUS_APPLICATION_SPECIFIC (0x03)
        assertTrue(Status.isApplicationSpecific(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC))

        // Test values in the application specific range (0x0B..0x0F)
        assertTrue(Status.isApplicationSpecific(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_START))
        assertTrue(Status.isApplicationSpecific(TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_END))
        assertTrue(Status.isApplicationSpecific(0x0Cu))
        assertTrue(Status.isApplicationSpecific(0x0Du))
        assertTrue(Status.isApplicationSpecific(0x0Eu))

        // Test random application-specific values
        repeat(10) {
            val randomValue = generateRandomApplicationSpecificByte()
            assertTrue(
                Status.isApplicationSpecific(randomValue),
                "Random value 0x${randomValue.toString(16)} should be application specific",
            )
        }
    }

    @Test
    fun testIsApplicationSpecificUnhappyPath() {
        // Test standard status values
        assertFalse(Status.isApplicationSpecific(TokenStatusListSpec.STATUS_VALID))
        assertFalse(Status.isApplicationSpecific(TokenStatusListSpec.STATUS_INVALID))
        assertFalse(Status.isApplicationSpecific(TokenStatusListSpec.STATUS_SUSPENDED))

        // Test values outside the application specific range
        assertFalse(Status.isApplicationSpecific(0x04u))
        assertFalse(Status.isApplicationSpecific(0x0Au))
        assertFalse(Status.isApplicationSpecific(0x10u))
    }
}
