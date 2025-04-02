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
package eu.europa.ec.eudi.statium.jose

/**
 * [JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
 */
public object RFC7519 {
    public const val ISSUER: String = "iss"
    public const val SUBJECT: String = "sub"
    public const val AUDIENCE: String = "aud"
    public const val EXPIRATION_TIME: String = "exp"
    public const val NOT_BEFORE: String = "nbf"
    public const val ISSUED_AT: String = "iat"
    public const val JWT_ID: String = "jti"
}
