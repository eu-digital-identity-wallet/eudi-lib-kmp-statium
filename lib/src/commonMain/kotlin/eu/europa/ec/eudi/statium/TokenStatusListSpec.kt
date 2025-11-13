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

/**
 * [Token Status List](https://www.ietf.org/archive/id/draft-ietf-oauth-status-list-10.html)
 */
public object TokenStatusListSpec {
    public const val VERSION: String = "draft-10"

    public const val STATUS: String = "status"
    public const val STATUS_LIST: String = "status_list"
    public const val IDX: String = "idx"
    public const val URI: String = "uri"
    public const val BITS: String = "bits"
    public const val LIST: String = "lst"
    public const val AGGREGATION_URI: String = "aggregation_uri"
    public const val TIME_TO_LIVE: String = "ttl"
    public const val TIME: String = "time"

    public const val TIME_TO_LIVE_COSE: Long = 65534
    public const val STATUS_LIST_COSE: Long = 65533

    public const val STATUS_VALID: UByte = 0x00u
    public const val STATUS_INVALID: UByte = 0x01u
    public const val STATUS_SUSPENDED: UByte = 0x02u
    public const val STATUS_APPLICATION_SPECIFIC: UByte = 0x03u
    public const val STATUS_APPLICATION_SPECIFIC_RANGE_START: UByte = 0x0Bu
    public const val STATUS_APPLICATION_SPECIFIC_RANGE_END: UByte = 0x0Fu

    public const val MEDIA_SUBTYPE_STATUS_LIST_JWT: String = "statuslist+jwt"
    public const val MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT: String = "application/$MEDIA_SUBTYPE_STATUS_LIST_JWT"
    public const val MEDIA_SUBTYPE_STATUS_LIST_CWT: String = "statuslist+cwt"
    public const val MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT: String = "application/$MEDIA_SUBTYPE_STATUS_LIST_CWT"
}
