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

import eu.europa.ec.eudi.statium.jose.RFC7519
import kotlinx.datetime.Instant
import kotlin.time.Duration

public interface StatusListTokenValidations {

    public fun StatusListTokenClaims.ensureValid(
        expectedSubject: String,
        validationTime: Instant,
        allowedClockSkew: Duration,
    ): StatusListTokenClaims =
        apply {
            ensureSubject(expectedSubject)
            ensureIssuedBefore(validationTime, allowedClockSkew)
            ensureNotExpired(validationTime, allowedClockSkew)
        }

    public fun StatusListTokenClaims.ensureIssuedBefore(validationTime: Instant, allowedClockSkew: Duration): StatusListTokenClaims =
        apply {
            val adjustedValidationTime = validationTime + allowedClockSkew
            check(issuedAt <= adjustedValidationTime) {
                "Status list token issued ($issuedAt) after validation time: $validationTime (with clock skew: $allowedClockSkew)"
            }
        }

    public fun StatusListTokenClaims.ensureNotExpired(validationTime: Instant, allowedClockSkew: Duration): StatusListTokenClaims =
        apply {
            if (expirationTime != null) {
                val adjustedValidationTime = validationTime - allowedClockSkew
                check(expirationTime >= adjustedValidationTime) {
                    "Status list token expired ($expirationTime) for validation time: $validationTime (with clock skew: $allowedClockSkew)"
                }
            }
        }

    public fun StatusListTokenClaims.ensureSubject(expectedSubject: String): StatusListTokenClaims =
        apply {
            check(expectedSubject == subject) {
                "Wrong `${RFC7519.SUBJECT}` claim. Expected: `$expectedSubject`, actual: `$subject`"
            }
        }

    public companion object : StatusListTokenValidations
}
