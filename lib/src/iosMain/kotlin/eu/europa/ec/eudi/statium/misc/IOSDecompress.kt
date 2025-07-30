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
 * Implementation of [Decompress] for Apple platforms using the built-in
 * zip utilities.
 */
public class IOSDecompress : Decompress {
    /**
     * Decompresses the given byte array using ZLIB/DEFLATE.
     *
     * @param bytes The compressed byte array
     * @return The decompressed byte array
     * @throws Exception if decompression fails
     */
    override suspend fun invoke(bytes: CompressedByteArray): ByteArray {
        val buffer = decompressZlib(bytes) ?: throw IllegalArgumentException("Failed to decompress: input may not be valid ZLIB format")
        return buffer
    }

    @OptIn(ExperimentalForeignApi::class)
    public fun decompressZlib(data: ByteArray): ByteArray? = memScoped {
        if (data.isEmpty()) return null

        val strm = alloc<z_stream>().apply {
            zalloc = null
            zfree = null
            opaque = null
            avail_in = data.size.toUInt()
            next_in = data.refTo(0).getPointer(this@memScoped).reinterpret()
        }

        // Use ZLIB header (15), not GZIP (15 + 16), not raw deflate (-15)
        if (inflateInit2_(strm.ptr, 15, ZLIB_VERSION, sizeOf<z_stream>().toInt()) != Z_OK) {
            return null
        }

        val out = ByteArrayOutput()
        val bufferSize = 16 * 1024
        val buffer = ByteArray(bufferSize)

        try {
            while (true) {
                strm.next_out = buffer.refTo(0).getPointer(this).reinterpret()
                strm.avail_out = buffer.size.toUInt()

                val result = inflate(strm.ptr, Z_NO_FLUSH)

                val bytesDecompressed = bufferSize - strm.avail_out.toInt()
                if (bytesDecompressed > 0) {
                    out.write(buffer, 0, bytesDecompressed)
                }

                when (result) {
                    Z_STREAM_END -> break
                    Z_OK -> continue
                    else -> return null // Error in stream
                }
            }
        } finally {
            inflateEnd(strm.ptr)
        }

        return out.toByteArray()
    }
}

public class ByteArrayOutput {
    private var buffer = ByteArray(1024)
    private var size = 0

    public fun write(src: ByteArray, offset: Int, length: Int) {
        ensureCapacity(size + length)
        src.copyInto(buffer, destinationOffset = size, startIndex = offset, endIndex = offset + length)
        size += length
    }

    public fun toByteArray(): ByteArray = buffer.copyOf(size)

    private fun ensureCapacity(required: Int) {
        if (required > buffer.size) {
            val newSize = maxOf(buffer.size * 2, required)
            buffer = buffer.copyOf(newSize)
        }
    }
}
