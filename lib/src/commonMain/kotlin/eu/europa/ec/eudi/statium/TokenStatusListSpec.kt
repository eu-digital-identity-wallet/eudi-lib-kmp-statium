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

    public const val STATUS_VALID: Byte = 0x00
    public const val STATUS_INVALID: Byte = 0x01
    public const val STATUS_SUSPENDED: Byte = 0x02
    public const val STATUS_APPLICATION_SPECIFIC: Byte = 0x03
    public const val STATUS_APPLICATION_SPECIFIC_RANGE_START: Byte = 0x0B
    public const val STATUS_APPLICATION_SPECIFIC_RANGE_END: Byte = 0x0F

    public const val MEDIA_SUBTYPE_STATUS_LIST_JWT: String = "statuslist+jwt"
    public const val MEDIA_TYPE_APPLICATION_STATUS_LIST_JWT: String = "application/$MEDIA_SUBTYPE_STATUS_LIST_JWT"
    public const val MEDIA_SUBTYPE_STATUS_LIST_CWT: String = "statuslist+cwt"
    public const val MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT: String = "application/$MEDIA_SUBTYPE_STATUS_LIST_CWT"
}
