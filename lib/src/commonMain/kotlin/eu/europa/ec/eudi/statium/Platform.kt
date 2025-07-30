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

import eu.europa.ec.eudi.statium.misc.Decompress
import io.ktor.client.HttpClient
import kotlin.coroutines.CoroutineContext

/**
 * Returns a platform-specific CoroutineContext suitable for IO operations
 */
internal expect fun platformIoContext(): CoroutineContext

/**
 * Creates a platform-specific Decompress implementation
 */
internal expect fun platformDecompress(context: CoroutineContext): Decompress

/**
 * Creates a platform-specific Decompress implementation with the default IO context
 */
internal fun platformDecompress(): Decompress = platformDecompress(platformIoContext())

public expect fun platformNonFatal(throwable: Throwable): Boolean

/**
 * Creates a platform-specific http client
 */
internal expect fun platformHttpClient(): HttpClient
