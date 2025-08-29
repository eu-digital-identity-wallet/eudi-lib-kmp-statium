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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * Compresses a [ByteArray] using ZLIB/DEFLATE on JVM and Android.
 * Uses the highest available compression level as recommended.
 */
internal class JvmAndAndroidCompress(private val context: CoroutineContext = Dispatchers.IO) : Compress {
    override suspend fun invoke(bytes: ByteArray): CompressedByteArray = withContext(context) {
        val outputStream = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_COMPRESSION, false)
        try {
            DeflaterOutputStream(outputStream, deflater).use { deflaterStream ->
                deflaterStream.write(bytes)
                deflaterStream.finish()
            }
            outputStream.toByteArray()
        } catch (e: Exception) {
            throw IOException("Compression failed", e)
        } finally {
            deflater.end()
        }
    }
}
