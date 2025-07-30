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
package eu.europa.ec.eudi.statium.http

import eu.europa.ec.eudi.statium.StatusListTokenFormat
import eu.europa.ec.eudi.statium.TokenStatusListSpec
import eu.europa.ec.eudi.statium.misc.runCatchingCancellable
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Instant

public interface GetStatusListTokenKtorOps {

    /**
     * Retrieves the token status list given a [uri] and a [format].
     * Optionally a [time in point][at] be specified
     */
    public suspend fun HttpClient.getStatusListToken(
        uri: String,
        format: StatusListTokenFormat,
        at: Instant?,
    ): Result<String> =
        runCatchingCancellable {
            val httpResponse = get(uri) {
                accept(format.contentType())
                at?.let { parameter(TokenStatusListSpec.TIME, it.epochSeconds) }
            }
            when {
                httpResponse.status.isSuccess() -> httpResponse.bodyAsText()
                else -> error("Got status ${httpResponse.status} while calling $uri")
            }
        }

    public companion object : GetStatusListTokenKtorOps {

        public fun StatusListTokenFormat.contentType(): ContentType {
            val value = when (this) {
                StatusListTokenFormat.JWT -> TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT
                StatusListTokenFormat.CWT -> TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT
            }
            return ContentType.Companion.parse(value)
        }
    }
}
