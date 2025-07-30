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
import eu.europa.ec.eudi.statium.misc.runCatchingCancellable
import kotlinx.datetime.Instant

/**
 * Allows the caller to get the status of a [`status_list`][StatusReference],
 * found in a referenced token.
 */
public fun interface GetStatus {
    /**
     * Gets the status of the [StatusReference], optionally specifying a [time point][at].
     *
     * Note: there are some privacy preserving considerations if [at] is provided, since
     * it can cause a time point information to be included to the query passed
     * to the Status list token Provider.
     *
     * It is recommended to use [currentStatus]
     *
     * @receiver The reference to the status, found in a Referenced Token
     * @param at the time point to check the status.
     * @return the status read
     */
    public suspend fun StatusReference.status(at: Instant?): Result<Status>

    /**
     * Gets the status of the [StatusReference] at present time
     * @receiver The reference to the status, found in a Referenced Token
     * @return the status read
     * */
    public suspend fun StatusReference.currentStatus(): Result<Status> = status(at = null)

    public companion object {

        /**
         * Factory method for creating an instance of [GetStatus],
         *
         * @param getStatusListToken a way to fetch the Status List token
         * @param decompress a way to decompress the status list contents. If not provided defaults to [platformDecompress]
         * @return an instance of [GetStatus]
         */
        public operator fun invoke(
            getStatusListToken: GetStatusListToken,
            decompress: Decompress = platformDecompress(),
        ): GetStatus = GetStatus { at ->
            runCatchingCancellable {
                val statusListTokenClaims = getStatusListToken(uri, at).getOrThrow()
                val statusList = statusListTokenClaims.statusList
                val readStatus = ReadStatus.fromStatusList(statusList, decompress).getOrThrow()
                readStatus(index).getOrThrow()
            }
        }
    }
}
