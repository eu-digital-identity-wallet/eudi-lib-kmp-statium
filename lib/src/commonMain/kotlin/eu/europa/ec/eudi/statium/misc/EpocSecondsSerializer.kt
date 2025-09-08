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
package eu.europa.ec.eudi.statium.misc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.CborDecoder
import kotlinx.serialization.cbor.CborEncoder
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Instant

/**
 * Serializes an [Instant] as a [Long] representing the [Instant.epochSeconds]
 */
internal object EpocSecondsSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("eu.europa.ec.eudi.statium.misc.EpocSecondsSerializer", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Instant = Instant.fromEpochSeconds(decoder.decodeLong())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeLong(value.epochSeconds)
    }
}

internal object EpocSecondsCborSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("eu.europa.ec.eudi.statium.misc.EpocSecondsCborSerializer", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Instant) {
        require(encoder is CborEncoder) { "This serializer can only be used with CBOR encoding" }

        // For now, just encode the epoch seconds as a long value
        // TODO: Add CBOR tag 1 encoding when the method is available
        encoder.encodeLong(value.epochSeconds)
    }

    override fun deserialize(decoder: Decoder): Instant {
        require(decoder is CborDecoder) { "This serializer can only be used with CBOR encoding" }

        // For now, just decode the epoch seconds as a long value
        // TODO: Add CBOR tag 1 decoding when the method is available
        return Instant.fromEpochSeconds(decoder.decodeLong())
    }
}
