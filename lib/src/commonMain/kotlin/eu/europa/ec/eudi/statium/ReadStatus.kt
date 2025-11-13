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

import eu.europa.ec.eudi.statium.BitsPerStatus.*
import eu.europa.ec.eudi.statium.misc.Decompress
import eu.europa.ec.eudi.statium.misc.runCatchingCancellable

/**
 * Reads a status at a specific [index][StatusIndex]
 */
public fun interface ReadStatus {

    public operator fun invoke(index: StatusIndex): Result<Status>

    public companion object {

        /**
         * Creates a [ReadStatus] instance, for a [statusList] that is represented as a [ByteArray]
         * The [statusList] is assumed base64 URL safe decoded and decompressed
         */
        public fun fromByteArray(bitsPerStatus: BitsPerStatus, statusList: ByteArray): ReadStatus =
            ReadStatus { index ->
                runCatchingCancellable {
                    with(bitsPerStatus) {
                        val (bytePosition, bitPosition) = byteAndBitPosition(index)
                        val byte = statusList[bytePosition]
                        readStatusByte(byte, bitPosition).getOrThrow()
                    }
                }
            }

        /**
         * Creates a [ReadStatus] instance, for a [statusList], given a [Decompress] function
         */
        public suspend fun fromStatusList(
            statusList: StatusList,
            decompress: Decompress = platformDecompress(),
        ): Result<ReadStatus> =
            runCatchingCancellable {
                val decompressedList = decompress(statusList.compressedList)
                fromByteArray(statusList.bytesPerStatus, decompressedList)
            }
    }
}

/**
 * Given an [index] for a status, calculates two positions:
 * - The position of the byte within the list where the status can be found and
 * - The position of the starting bit for the status, within the byte
 *
 * To read the status, you should read the byte at the position returned on the left of the result,
 * then locate the bit (at the right of the result) and finally read as many bits as the [BitsPerStatus]
 *
 * @receiver The bits for the status encoding
 * @param index the index of the status within the Status List
 * @return the position of the byte and the bit to be read
 *
 */
public fun BitsPerStatus.byteAndBitPosition(index: StatusIndex): Pair<Int, Int> {
    val bytePosition = index.value / statusesPerByte
    val bitPosition = run {
        val positionInByte = index.value % statusesPerByte
        positionInByte * bits
    }
    return bytePosition to bitPosition
}

/**
 * Attempts to read from a [byte of the status list][statusByte] the status
 * that is located at [bitPosition]
 *
 * @receiver The bits for the status encoding
 * @param statusByte The byte from which to extract the stratus
 * @param bitPosition The bit position within the [statusByte] where the status begins
 *
 * @return the status read
 */
public fun BitsPerStatus.readStatusByte(statusByte: Byte, bitPosition: Int): Result<Status> = runCatchingCancellable {
    require(bitPosition in 0..<BitsPerStatus.BITS_PER_BYTE) {
        "Bit position should be in range [0,${BitsPerStatus.BITS_PER_BYTE})"
    }
    val baseMask: Int = when (this) {
        One -> 0b00000001
        Two -> 0b00000011
        Four -> 0b00001111
        Eight -> 0b11111111
    }
    val statusValue = ((statusByte.toInt() and (baseMask shl bitPosition)) shr bitPosition)
    Status(this, statusValue.toUByte()).getOrThrow()
}
