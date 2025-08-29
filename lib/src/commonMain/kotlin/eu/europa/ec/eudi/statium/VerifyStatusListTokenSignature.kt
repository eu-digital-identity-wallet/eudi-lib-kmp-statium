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

import kotlin.time.Instant

/**
 * Verifies the status list token signature
 */
public fun interface VerifyStatusListTokenSignature {
    /**
     * Verifies the signature of a status list at a specific [point in time][at].
     * It raises an exception in case of invalid signature
     */
    public suspend operator fun invoke(
        statusListToken: String,
        format: StatusListTokenFormat,
        at: Instant,
    ): Result<Unit>
}

public fun interface VerifyStatusListTokenJwtSignature {
    /**
     * Verifies the signature of a status list at a specific [point in time][at].
     * It raises an exception in case of invalid signature
     */
    public suspend operator fun invoke(
        statusListToken: String,
        at: Instant,
    ): Result<Unit>

    public companion object {
        internal val Ignore: VerifyStatusListTokenJwtSignature = VerifyStatusListTokenJwtSignature { _, _ ->
            Result.success(Unit)
        }
    }
}

public fun interface VerifyStatusListTokenCwtSignature {
    /**
     * Verifies the signature of a status list at a specific [point in time][at].
     * It raises an exception in case of invalid signature
     */
    public suspend operator fun invoke(
        statusListToken: ByteArray,
        at: Instant,
    ): Result<Unit>

    public companion object {
        internal val Ignore: VerifyStatusListTokenCwtSignature = VerifyStatusListTokenCwtSignature { _, _ ->
            Result.success(Unit)
        }
    }
}
