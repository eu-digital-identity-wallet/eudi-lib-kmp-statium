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
import kotlinx.cinterop.*
import platform.zlib.*

/**
 * Implementation of [Decompress] for JVM and Android platforms using Java's built-in
 * zip utilities.
 */
internal class IOSDecompress : Decompress {
    /**
     * Decompresses the given byte array using ZLIB/DEFLATE.
     *
     * @param bytes The compressed byte array
     * @return The decompressed byte array
     * @throws Exception if decompression fails
     */
    override suspend fun invoke(bytes: CompressedByteArray): ByteArray {
        val (len, buffer) = decompressGzip(bytes)
        val clampedBuffer = buffer.copyOf(len.coerceAtMost(buffer.size))
        return clampedBuffer
    }

    @OptIn(ExperimentalForeignApi::class)
    internal fun decompressGzip(data: ByteArray): Pair<Int, ByteArray> = memScoped {
        // Estimate a reasonable buffer size; adjust as needed
        val destinationLength = 1024UL * 8UL
        val destinationBuffer = ByteArray(destinationLength.toInt())

        val destinationLengthVar = alloc<ULongVar>()
        val result = data.usePinned { inputPinned ->
            destinationBuffer.usePinned { outputPinned ->
                memScoped {

                    destinationLengthVar.value = destinationLength

                    uncompress(
                        outputPinned.addressOf(0).reinterpret(),
                        destinationLengthVar.ptr,
                        inputPinned.addressOf(0).reinterpret(),
                        data.size.convert()
                    )
                }
            }
        }

        if (result != Z_OK) {
            throw RuntimeException("Decompression failed with zlib error code: $result")
        }

        // Return only the decompressed portion
        return destinationLengthVar.value.toInt() to destinationBuffer.copyOf(destinationLength.toInt())
    }
}
