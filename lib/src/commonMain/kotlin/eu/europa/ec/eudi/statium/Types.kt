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

import eu.europa.ec.eudi.statium.Status.Companion.applicationSpecificRange
import eu.europa.ec.eudi.statium.Status.Companion.isApplicationSpecific
import eu.europa.ec.eudi.statium.jose.RFC7519
import eu.europa.ec.eudi.statium.misc.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the number of [bits] for a status.
 */
@Serializable(with = BitsPerStatusSerializer::class)
public enum class BitsPerStatus(public val bits: Int) {
    One(1),
    Two(2),
    Four(4),
    Eight(8),
    ;

    /**
     * The number of statuses that can be represented
     * by a single byte
     */
    public val statusesPerByte: Int get() = BITS_PER_BYTE / bits

    public companion object {
        internal const val BITS_PER_BYTE = 8

        /**
         * Gets a [BitsPerStatus]
         * given the number of [bits]
         */
        public fun forBits(bits: Int): BitsPerStatus? {
            return BitsPerStatus.entries.find { it.bits == bits }
        }
    }
}

/**
 * Represents the index to check for status information in the Status List
 * The [value] MUST be a non-negative number, zero or greater
 */
@Serializable
@JvmInline
public value class StatusIndex(public val value: Int) {
    init {
        require(value >= 0) { "The value MUST be a non-negative number, zero or greater.: $value" }
    }

    public override fun toString(): String = value.toString()
}

/**
 * A reference to a status, as it would appear in a referenced token (credential)
 * nested inside a status claim, under the attribute 'status_list'
 *
 * @param index It MUST specify an Integer that represents the index to check
 * for status information in the Status List for the current Referenced Token.
 * @param uri It MUST specify a String value that identifies the Status List Token
 * containing the status information for the Referenced Token
 */
@Serializable
public data class StatusReference(
    @SerialName(TokenStatusListSpec.IDX) @Required public val index: StatusIndex,
    @SerialName(TokenStatusListSpec.URI) @Required public val uri: String,
) {
    init {
        require(uri.isNotBlank()) { "The uri must not be empty." }
    }
}

public enum class StatusListTokenFormat {
    JWT,
    CWT,
}

public typealias CompressedByteArray = @Contextual ByteArray

/**
 * Status list representation
 *
 * @param bytesPerStatus The number of bits per Referenced Token in the Status List
 * @param compressedList The compressed status values for all the Referenced Tokens it conveys statuses for
 * @param aggregationUri A URI to retrieve the Status List Aggregation for this type of Referenced Token or Issuer
 */
@Serializable
public data class StatusList(
    @SerialName(TokenStatusListSpec.BITS) @Required val bytesPerStatus: BitsPerStatus,
    @SerialName(TokenStatusListSpec.LIST) @Required val compressedList: CompressedByteArray,
    @SerialName(TokenStatusListSpec.AGGREGATION_URI) val aggregationUri: String? = null,
) {
    public companion object {

        /**
         * Attempts to create a [eu.europa.ec.eudi.statium.StatusList]
         * It jus decodes the [base64UrlEncodedList]
         *
         * @param bytesPerStatus  The number of bits per Referenced Token in the Status List
         * @param base64UrlEncodedList The Base64 URL no padding encoded, compressed list
         * @param aggregationUri  A URI to retrieve the Status List Aggregation for this type of Referenced Token or Issuer
         * @return the [eu.europa.ec.eudi.statium.StatusList] if given [base64UrlEncodedList] can be base64-decoded
         */
        public fun fromBase64UrlEncodedList(
            bytesPerStatus: BitsPerStatus,
            base64UrlEncodedList: String,
            aggregationUri: String? = null,
        ): Result<StatusList> =
            runCatching { StatusList(bytesPerStatus, Base64UrlNoPadding.decode(base64UrlEncodedList), aggregationUri) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StatusList) return false

        if (bytesPerStatus != other.bytesPerStatus) return false
        if (!compressedList.contentEquals(other.compressedList)) return false
        if (aggregationUri != other.aggregationUri) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytesPerStatus.hashCode()
        result = 31 * result + compressedList.fold(0) { acc, byte -> 31 * acc + byte.toInt() }
        result = 31 * result + (aggregationUri?.hashCode() ?: 0)
        return result
    }
}

@Serializable
@JvmInline
public value class TimeToLive(public val value: DurationAsSeconds) {
    init {
        require(value.isPositive()) { "Time to live value must be positive" }
    }

    override fun toString(): String = value.toString()
}

/**
 * The claims of a Status List Token
 */
@Serializable
public data class StatusListTokenClaims(
    @SerialName(RFC7519.SUBJECT) @Required val subject: String,
    @SerialName(RFC7519.ISSUED_AT) @Required val issuedAt: InstantAsEpocSeconds,
    @SerialName(RFC7519.EXPIRATION_TIME) val expirationTime: InstantAsEpocSeconds? = null,
    @SerialName(TokenStatusListSpec.TIME_TO_LIVE) val timeToLive: TimeToLive? = null,
    @SerialName(TokenStatusListSpec.STATUS_LIST) val statusList: StatusList,
) {
    init {
        require(subject.isNotBlank()) { "The subject must not be empty." }
    }
}

/**
 * The registered status types
 */
public sealed interface Status {

    /**
     * Indicates a valid referenced token
     * It is available for a [BitsPerStatus]
     */
    public data object Valid : Status

    /**
     * Indicates an invalid referenced token
     * It is available for a [BitsPerStatus]
     */
    public data object Invalid : Status

    /**
     * A suspended referenced token
     * It is available for [BitsPerStatus] higher than [BitsPerStatus.One]
     */
    public data object Suspended : Status

    /**
     * An application-specific status expressed by [value]
     *
     * It is available for [BitsPerStatus] higher than [BitsPerStatus.One]
     *
     * Check [isApplicationSpecific] for the restrictions of [value]
     */
    public data class ApplicationSpecific internal constructor(val value: Byte) : Status

    /**
     * Statuses reserved for future registration.
     * The [value] is not [ApplicationSpecific] neither [Valid], nor [Invalid]
     */
    public data class Reserved internal constructor(val value: Byte) : Status

    /**
     * The value the status in [Byte]
     */
    public fun toByte(): Byte = when (this) {
        Valid -> TokenStatusListSpec.STATUS_VALID
        Invalid -> TokenStatusListSpec.STATUS_INVALID
        Suspended -> TokenStatusListSpec.STATUS_SUSPENDED
        is ApplicationSpecific -> value
        is Reserved -> value
    }

    public companion object {
        /**
         * Creates a [Status] given a [value].
         */
        public operator fun invoke(value: Byte): Status = when {
            value == TokenStatusListSpec.STATUS_VALID -> Valid
            value == TokenStatusListSpec.STATUS_INVALID -> Invalid
            value == TokenStatusListSpec.STATUS_SUSPENDED -> Suspended
            isApplicationSpecific(value) -> ApplicationSpecific(value)
            else -> Reserved(value.toByte())
        }

        private val applicationSpecificRange =
            TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_START..TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_END

        /**
         * According to specification returns true in case the given [value]
         * is [TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC] or in the [applicationSpecificRange]
         */
        public fun isApplicationSpecific(value: Byte): Boolean =
            value == TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC ||
                value in applicationSpecificRange
    }
}
