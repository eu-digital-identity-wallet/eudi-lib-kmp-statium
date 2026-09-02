/*
 * Copyright (c) 2025-2026 European Commission
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import kotlin.coroutines.CoroutineContext

/**
 * Implementation of [Decompress] for JVM and Android platforms using Java's built-in
 * zip utilities.
 */
internal class JvmAndroidDecompress(private val context: CoroutineContext = Dispatchers.IO) : Decompress {
    /**
     * Decompresses the given byte array using ZLIB/DEFLATE.
     *
     * @param bytes The compressed byte array
     * @param maximumDecompressedSize The maximum allowed decompressed size, in bytes
     * @return The decompressed byte array
     * @throws Exception if decompression fails
     */
    override suspend fun invoke(
        bytes: CompressedByteArray,
        maximumDecompressedSize: UInt,
    ): ByteArray = withContext(context) {
        require(maximumDecompressedSize > 0u) {
            "maximumDecompressedSize must be greater than zero"
        }

        ByteArrayInputStream(bytes).use { inputStream ->
            val inflater = Inflater(false)
            try {
                InflaterInputStream(inputStream, inflater).use { inflaterStream ->
                    ByteArrayOutputStream().use { outputStream ->
                        var decompressedSize = 0
                        val buffer = ByteArray(BUFFER_SIZE)
                        do {
                            check(decompressedSize <= maximumDecompressedSize.toInt()) {
                                "Decompressed ByteArray exceeds maximum allowed size"
                            }
                            val read = inflaterStream.read(buffer)
                            if (-1 != read) {
                                outputStream.write(buffer, 0, read)
                                decompressedSize += read
                            }
                        } while (-1 != read)
                        outputStream.toByteArray()
                    }
                }
            } finally {
                inflater.end()
            }
        }
    }

    private companion object {
        /**
         * Size used for buffers during decompression i.e., 8KB.
         */
        private const val BUFFER_SIZE: Int = 8192
    }
}
