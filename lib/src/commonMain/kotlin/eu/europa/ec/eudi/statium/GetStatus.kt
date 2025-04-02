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
import kotlinx.datetime.Instant

/**
 * Allows the caller to get the status of a [`status_list`][StatusReference],
 * found in a referenced token.
 *
 */
public fun interface GetStatus {
    /**
     * Gets the status of the [StatusReference]
     * A [time point][at] can be specified
     */
    public suspend fun StatusReference.status(at: Instant?): Result<Status>

    public companion object {

        /**
         * Factory method for creating an instance of [GetStatus],
         * given a [way][getStatusListToken] to fetch the Status List token
         * and a [way][Decompress] to decompress its content
         */
        public operator fun invoke(
            getStatusListToken: GetStatusListToken,
            decompress: Decompress = platformDecompress(),
        ): GetStatus = GetStatus { at ->
            runCatching {
                val statusListTokenClaims = getStatusListToken(uri, at).getOrThrow()
                val statusList = statusListTokenClaims.statusList
                val readStatus = ReadStatus.fromStatusList(statusList, decompress).getOrThrow()
                readStatus(index).getOrThrow()
            }
        }
    }
}
