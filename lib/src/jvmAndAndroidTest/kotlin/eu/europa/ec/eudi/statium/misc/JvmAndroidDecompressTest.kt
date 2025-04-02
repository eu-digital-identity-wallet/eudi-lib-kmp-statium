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

import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.zip.DeflaterOutputStream
import kotlin.test.assertContentEquals

class JvmAndroidDecompressTest {

    @Test
    fun testDecompress() = runTest {
        val originalData = "Hello, this is a test of ZLIB compression and decompression!".encodeToByteArray()
        val compressedData = compressWithZlib(originalData)
        val decompress = JvmAndroidDecompress(coroutineContext)
        val decompressedData = decompress(compressedData)

        // Verify the decompressed data matches the original
        assertContentEquals(originalData, decompressedData)
    }

    /**
     * Helper function to compress data using ZLIB for testing
     */
    private fun compressWithZlib(data: ByteArray): ByteArray {
        val outputStream = ByteArrayOutputStream()
        DeflaterOutputStream(outputStream).use { deflaterStream ->
            deflaterStream.write(data)
            deflaterStream.finish()
        }
        return outputStream.toByteArray()
    }
}
