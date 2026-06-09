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
import eu.europa.ec.eudi.statium.misc.Compress
import eu.europa.ec.eudi.statium.misc.Decompress
import eu.europa.ec.eudi.statium.misc.IOSDecompress
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

internal actual fun platformDecompress(context: CoroutineContext): Decompress = IOSDecompress()

/**
 * Returns a platform-specific CoroutineContext suitable for IO operations
 */
internal actual fun platformIoContext(): CoroutineContext = Dispatchers.Default

internal actual fun platformCompress(context: CoroutineContext): Compress {
    TODO("Not yet implemented")
}

internal actual fun createHttpClient(): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }

        // Optional: tune iOS-specific networking behavior
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }
    }
}