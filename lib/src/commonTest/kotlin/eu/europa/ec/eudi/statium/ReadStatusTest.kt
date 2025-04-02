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

class ReadStatusTest {

    @Test
    fun testBitsPerStatusOneOps() {
        // Example 1: 1 bit per status
        // In this case, each bit represents a status (0 or 1)
        // Byte: 10101010 (binary) = 0xAA (hex) = 170 (decimal)
        // Status at index 0: 0 (rightmost bit)
        // Status at index 1: 1
        // Status at index 2: 0
        // Status at index 3: 1
        // Status at index 4: 0
        // Status at index 5: 1
        // Status at index 6: 0
        // Status at index 7: 1 (leftmost bit)
        val byteArray = byteArrayOf(0xAA.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.One, byteArray)

        // Using the ops directly
        assertEquals(0, readStatus(0).getOrNull())
        assertEquals(1, readStatus(1).getOrNull())
        assertEquals(0, readStatus(2).getOrNull())
        assertEquals(1, readStatus(3).getOrNull())
        assertEquals(0, readStatus(4).getOrNull())
        assertEquals(1, readStatus(5).getOrNull())
        assertEquals(0, readStatus(6).getOrNull())
        assertEquals(1, readStatus(7).getOrNull())
    }

    @Test
    fun testBitsPerStatusOneOps2() {
        // Test with multiple bytes
        // Byte 0: 10101010 (0xAA)
        // Byte 1: 11001100 (0xCC)
        val byteArray = byteArrayOf(0xAA.toByte(), 0xCC.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.One, byteArray)

        // First byte (index 0-7)
        assertEquals(0, readStatus(0).getOrNull())
        assertEquals(1, readStatus(1).getOrNull())
        assertEquals(0, readStatus(2).getOrNull())
        assertEquals(1, readStatus(3).getOrNull())
        assertEquals(0, readStatus(4).getOrNull())
        assertEquals(1, readStatus(5).getOrNull())
        assertEquals(0, readStatus(6).getOrNull())
        assertEquals(1, readStatus(7).getOrNull())

        // Second byte (index 8-15)
        assertEquals(0, readStatus(8).getOrNull())
        assertEquals(0, readStatus(9).getOrNull())
        assertEquals(1, readStatus(10).getOrNull())
        assertEquals(1, readStatus(11).getOrNull())
        assertEquals(0, readStatus(12).getOrNull())
        assertEquals(0, readStatus(13).getOrNull())
        assertEquals(1, readStatus(14).getOrNull())
        assertEquals(1, readStatus(15).getOrNull())
    }

    @Test
    fun testBitsPerStatusTwoOps() {
        // Example 2: 2 bits per status
        // In this case, each 2 bits represent a status (0-3)
        // Byte: 11100100 (binary) = 0xE4 (hex) = 228 (decimal)
        // Status at index 0: 00 (binary) = 0 (decimal)
        // Status at index 1: 01 (binary) = 1 (decimal)
        // Status at index 2: 10 (binary) = 2 (decimal)
        // Status at index 3: 11 (binary) = 3 (decimal)
        val byteArray = byteArrayOf(0xE4.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.Two, byteArray)

        assertEquals(0, readStatus(0).getOrNull())
        assertEquals(1, readStatus(1).getOrNull())
        assertEquals(2, readStatus(2).getOrNull())
        assertEquals(3, readStatus(3).getOrNull())
    }

    @Test
    fun testBitsPerStatusTwoOps2() {
        // Test with multiple bytes
        // Byte 0: 11100100 (0xE4)
        // Byte 1: 10011011 (0x9B)
        val byteArray = byteArrayOf(0xE4.toByte(), 0x9B.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.Two, byteArray)

        // First byte (index 0-3)
        assertEquals(0, readStatus(0).getOrNull())
        assertEquals(1, readStatus(1).getOrNull())
        assertEquals(2, readStatus(2).getOrNull())
        assertEquals(3, readStatus(3).getOrNull())

        // Second byte (index 4-7)
        assertEquals(3, readStatus(4).getOrNull())
        assertEquals(2, readStatus(5).getOrNull())
        assertEquals(1, readStatus(6).getOrNull())
        assertEquals(2, readStatus(7).getOrNull())
    }

    @Test
    fun testBitsPerStatusFourOps() {
        // Example 3: 4 bits per status
        // In this case, each 4 bits (nibble) represent a status (0-15)
        // Byte: 11110000 (binary) = 0xF0 (hex) = 240 (decimal)
        // Status at index 0: 0000 (binary) = 0 (decimal)
        // Status at index 1: 1111 (binary) = 15 (decimal)
        val byteArray = byteArrayOf(0xF0.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.Four, byteArray)

        assertEquals(0, readStatus(0).getOrNull())
        assertEquals(15, readStatus(1).getOrNull())
    }

    @Test
    fun testBitsPerStatusFourOps2() {
        // Test with multiple bytes
        // Byte 0: 11110000 (0xF0)
        // Byte 1: 10100101 (0xA5)
        val byteArray = byteArrayOf(0xF0.toByte(), 0xA5.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.Four, byteArray)

        // First byte (index 0-1)
        assertEquals(0, readStatus(0).getOrNull())
        assertEquals(15, readStatus(1).getOrNull())

        // Second byte (index 2-3)
        assertEquals(5, readStatus(2).getOrNull())
        assertEquals(10, readStatus(3).getOrNull())
    }

    @Test
    fun testBitsPerStatusEightOps() {
        // Example 4: 8 bits per status
        // In this case, each byte represents a status (0-255)
        // Byte 0: 11111111 (binary) = 0xFF (hex) = 255 (decimal)
        // Byte 1: 00000000 (binary) = 0x00 (hex) = 0 (decimal)
        // Byte 2: 10101010 (binary) = 0xAA (hex) = 170 (decimal)
        val byteArray = byteArrayOf(0xFF.toByte(), 0x00.toByte(), 0xAA.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.Eight, byteArray)

        assertEquals(255, readStatus(0).getOrNull())
        assertEquals(0, readStatus(1).getOrNull())
        assertEquals(170, readStatus(2).getOrNull())
    }

    @Test
    fun testNegativeIndexOps() {
        // Test that negative indices throw an IllegalArgumentException
        val byteArray = byteArrayOf(0x00.toByte())
        val readStatus = ReadStatus.fromByteArray(BitsPerStatus.One, byteArray)

        assertFailsWith<IllegalArgumentException> {
            readStatus(-1).getOrThrow()
        }
    }

    internal fun assertEquals(expected: Int, actual: Status?) {
        assertEquals(Status(expected.toByte()), actual)
    }
}

private operator fun ReadStatus.invoke(index: Int) = invoke(StatusIndex(index))
