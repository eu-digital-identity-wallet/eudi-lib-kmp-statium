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
package eu.europa.ec.eudi.statium

import eu.europa.ec.eudi.statium.misc.Compress
import eu.europa.ec.eudi.statium.misc.Decompress
import kotlin.coroutines.CoroutineContext

/*
* The default maximum allowed decompression size i.e., 16MB.
*/
public const val DEFAULT_MAXIMUM_DECOMPRESSED_SIZE: UInt = 16777216u

/**
 * Returns a platform-specific CoroutineContext suitable for IO operations
 */
internal expect fun platformIoContext(): CoroutineContext

/**
 * Creates a platform-specific Decompress implementation
 *
 * @param maximumDecompressedSize The maximum allowed decompressed size in bytes; Defaults to [DEFAULT_MAXIMUM_DECOMPRESSED_SIZE]
 */
internal expect fun platformDecompress(context: CoroutineContext, maximumDecompressedSize: UInt): Decompress

/**
 * Creates a platform-specific Decompress implementation with the default IO context with a 16MB limit
 */
internal fun platformDecompress(): Decompress = platformDecompress(platformIoContext(), DEFAULT_MAXIMUM_DECOMPRESSED_SIZE)

internal expect fun platformCompress(context: CoroutineContext): Compress

internal fun platformCompress(): Compress = platformCompress(platformIoContext())
