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

import eu.europa.ec.eudi.statium.BitsPerStatus
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object BitsPerStatusSerializer : KSerializer<BitsPerStatus> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("eu.europa.ec.eudi.statium.misc.BitsPerStatus", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: BitsPerStatus) {
        encoder.encodeInt(value.bits)
    }

    override fun deserialize(decoder: Decoder): BitsPerStatus {
        val bits = decoder.decodeInt()
        return BitsPerStatus.forBits(bits)
            ?: throw SerializationException("Unknown BitsPerStatus value: $bits")
    }
}
