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
import eu.europa.ec.eudi.statium.misc.IOSDecompress
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual fun platformDecompress(context: CoroutineContext): Decompress = IOSDecompress()

/**
 * Returns a platform-specific CoroutineContext suitable for IO operations
 */
internal actual fun platformIoContext(): CoroutineContext = Dispatchers.Default

public actual fun platformNonFatal(throwable: Throwable): Boolean =
    when (throwable) {
        is CancellationException -> false
        else -> true
    }

internal actual fun platformHttpClient(): HttpClient = HttpClient(Darwin)
