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
package eu.europa.ec.eudi.statium.cose

/**
 * [RFC8392 - CBOR Web Token (CWT)](https://datatracker.ietf.org/doc/html/rfc8392)
 */
public object RFC8392 {
    public const val SUBJECT: Long = 2
    public const val ISSUED_AT: Long = 6
    public const val EXPIRATION_TIME: Long = 4
}
