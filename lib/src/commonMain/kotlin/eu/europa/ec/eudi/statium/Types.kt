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
import eu.europa.ec.eudi.statium.misc.Base64UrlNoPadding
import eu.europa.ec.eudi.statium.misc.BitsPerStatusSerializer
import eu.europa.ec.eudi.statium.misc.EpocSecondsSerializer
import eu.europa.ec.eudi.statium.misc.StatiumJsonSerializersModule
import eu.europa.ec.eudi.statium.misc.runCatchingCancellable
import kotlinx.serialization.Contextual
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborLabel
import kotlin.time.Duration
import kotlin.time.Instant

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
         * Attempts to get aa [BitsPerStatus], given the number of [bits]
         * @param bits number of bits
         * @return the [BitsPerStatus] or null
         */
        public fun fromBitsOrNull(bits: Int): BitsPerStatus? = BitsPerStatus.entries.find { it.bits == bits }
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

/**
 * A type alias for a compressed [ByteArray] that contains
 * How this type is serialized depends on the serialization format
 *
 * @see StatiumJsonSerializersModule for JSON serialization
 */
public typealias CompressedByteArray = @Contextual ByteArray

/**
 * Status list representation
 *
 * @param bytesPerStatus The number of bits per Referenced Token in the Status List
 * @param compressedList The compressed status values for all the Referenced Tokens it conveys statuses for
 * @param aggregationUri A URI to retrieve the Status List Aggregation for this type of Referenced Token or Issuer
 *
 * @see StatiumJsonSerializersModule for JSON serialization
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
         * @return the status list
         */
        public fun fromBase64UrlEncodedList(
            bytesPerStatus: BitsPerStatus,
            base64UrlEncodedList: String,
            aggregationUri: String? = null,
        ): Result<StatusList> =
            runCatchingCancellable {
                val compressedList = Base64UrlNoPadding.decode(base64UrlEncodedList)
                StatusList(bytesPerStatus, compressedList, aggregationUri)
            }
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

/**
 * An [Instant] that will be serialized using [EpocSecondsSerializer]
 * Can be used for claims like "exp", "iat", "nbf"
 * @see StatiumJsonSerializersModule for JSON serialization
 */
public typealias InstantAsEpocSeconds = @Contextual Instant

/**
 * A [Duration] that is represented as seconds when serialized
 * @see StatiumJsonSerializersModule for JSON serialization
 */
public typealias DurationAsSeconds = @Contextual Duration

/**
 * A time-to-live for the [StatusListTokenClaims]
 * @param value a positive duration
 * @see StatiumJsonSerializersModule for JSON serialization
 */
@Serializable
@JvmInline
public value class PositiveDurationAsSeconds(public val value: DurationAsSeconds) {
    init {
        require(value.isPositive()) { "Time to live value must be positive" }
    }

    override fun toString(): String = value.toString()
}

/**
 * The claims of a Status List Token
 *
 * @param subject The token's subject. It should be a URI from which the status was retrieved
 * @param issuedAt The time when the token was issued
 * @param expirationTime The time when the token expires
 * @param timeToLive The duration that the token can cached
 * @param statusList The Status List
 *
 * @see StatiumJsonSerializersModule for JSON serialization
 */
@Serializable
public data class StatusListTokenClaims
@Throws(IllegalStateException::class)
public constructor(
    @CborLabel(2) @SerialName(RFC7519.SUBJECT) @Required val subject: String,
    @CborLabel(6) @SerialName(RFC7519.ISSUED_AT) @Required @Contextual val issuedAt: InstantAsEpocSeconds,
    @CborLabel(4) @SerialName(RFC7519.EXPIRATION_TIME) @Contextual val expirationTime: InstantAsEpocSeconds? = null,
    @CborLabel(65534) @SerialName(TokenStatusListSpec.TIME_TO_LIVE) val timeToLive: PositiveDurationAsSeconds? = null,
    @CborLabel(65533) @SerialName(TokenStatusListSpec.STATUS_LIST) val statusList: StatusList,
) {
    init {
        require(subject.isNotBlank()) { "The subject must not be empty." }
    }
}

/**
 * The registered status types
 */
public sealed interface Status : Comparable<Status> {

    /**
     * Indicates a valid referenced token
     * It is available for any [BitsPerStatus]
     */
    public data object Valid : Status

    /**
     * Indicates an invalid referenced token
     * It is available for any [BitsPerStatus]
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
    public data class ApplicationSpecific internal constructor(val value: UByte) : Status

    /**
     * Statuses reserved for future registration.
     * The [value] is not [ApplicationSpecific] neither [Valid], nor [Invalid]
     */
    public data class Reserved internal constructor(val value: UByte) : Status

    /**
     * The value the status in [UByte]
     */
    public fun toUByte(): UByte = when (this) {
        Valid -> TokenStatusListSpec.STATUS_VALID
        Invalid -> TokenStatusListSpec.STATUS_INVALID
        Suspended -> TokenStatusListSpec.STATUS_SUSPENDED
        is ApplicationSpecific -> value
        is Reserved -> value
    }

    @Deprecated(
        message = "This method will be removed in 0.3.x",
        replaceWith = ReplaceWith("toUByte().toByte()"),
    )
    public fun toByte(): Byte = toUByte().toByte()

    override fun compareTo(other: Status): Int = this.toUByte().compareTo(other.toUByte())

    public companion object {
        /**
         * Creates a [Status] given a [value].
         */
        public operator fun invoke(value: UByte): Status = when {
            value == TokenStatusListSpec.STATUS_VALID -> Valid
            value == TokenStatusListSpec.STATUS_INVALID -> Invalid
            value == TokenStatusListSpec.STATUS_SUSPENDED -> Suspended
            isApplicationSpecific(value) -> ApplicationSpecific(value)
            else -> Reserved(value)
        }

        /**
         * Attempts to create a [Status].
         * It will check that the given [statusValue] can be represented by the given [bitsPerStatus]
         *
         * @param bitsPerStatus the number of bits for representing the status
         * @param statusValue the value of the status
         */
        public operator fun invoke(bitsPerStatus: BitsPerStatus, statusValue: UByte): Result<Status> = runCatchingCancellable {
            val maxValue: UByte = when (bitsPerStatus) {
                BitsPerStatus.One -> 1u
                BitsPerStatus.Two -> 3u
                BitsPerStatus.Four -> 15u
                BitsPerStatus.Eight -> 255u
            }
            require(statusValue <= maxValue) {
                "Status $statusValue cannot be represented with ${bitsPerStatus.bits} bits"
            }
            Status(statusValue)
        }

        private val applicationSpecificRange =
            TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_START..TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC_RANGE_END

        /**
         * According to specification returns true in case the given [value]
         * is [TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC] or in the [applicationSpecificRange]
         */
        public fun isApplicationSpecific(value: UByte): Boolean =
            value == TokenStatusListSpec.STATUS_APPLICATION_SPECIFIC || value in applicationSpecificRange
    }
}
