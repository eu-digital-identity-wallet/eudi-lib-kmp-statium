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
 * [RFC8152 - COSE](https://datatracker.ietf.org/doc/html/rfc8152)
 */
public object RFC8152 {

    /**
     * Tag value for a COSE Single Signer Data Object
     */
    public const val COSE_SIGN1_TAG: ULong = 18u

    /**
     * Tag value for a COSE Mac w/o Recipients Object
     */
    public const val COSE_MAC0_TAG: ULong = 17u
}
