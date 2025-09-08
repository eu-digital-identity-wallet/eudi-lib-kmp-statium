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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.InflaterInputStream
import kotlin.test.assertContentEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class JvmAndAndroidCompressTest {

    @Test
    fun testCompress() = runTest {
        val originalData = "Hello, this is a test of ZLIB compression and decompression!".encodeToByteArray()

        val compress = JvmAndAndroidCompress(coroutineContext)
        val compressedData = compress(originalData)

        assertNotEquals(originalData.toList(), compressedData.toList(), "Compressed data should differ from original")

        val decompressed = decompressWithZlib(compressedData)
        assertContentEquals(originalData, decompressed)

        // For this string, compression should usually reduce size; be lenient but check it's not dramatically larger
        assertTrue(compressedData.size < originalData.size + 10, "Compressed size should not be significantly larger than original for this input")
    }

    private fun decompressWithZlib(data: ByteArray): ByteArray {
        ByteArrayInputStream(data).use { input ->
            InflaterInputStream(input).use { inflater ->
                val out = ByteArrayOutputStream()
                inflater.copyTo(out)
                return out.toByteArray()
            }
        }
    }
}
