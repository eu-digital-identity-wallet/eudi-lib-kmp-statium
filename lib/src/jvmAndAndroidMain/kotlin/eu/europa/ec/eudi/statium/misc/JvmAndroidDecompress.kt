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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
     * @return The decompressed byte array
     * @throws Exception if decompression fails
     */
    override suspend fun invoke(bytes: CompressedByteArray): ByteArray = withContext(context) {
        ByteArrayInputStream(bytes).use { inputStream ->
            InflaterInputStream(inputStream).use { inflaterStream ->
                ByteArrayOutputStream().use { outputStream ->
                    inflaterStream.copyTo(outputStream)
                    outputStream.toByteArray()
                }
            }
        }
    }
}
