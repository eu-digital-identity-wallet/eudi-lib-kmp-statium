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
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public typealias ByteArrayAsBase64UrlNoPadding =
    @Serializable(with = ByteArrayAsBase64UrlNoPaddingSerializer::class)
    ByteArray

/**
 * A parameterized serializer that converts between a type T and a Base64 URL-encoded (no padding) string.
 *
 * The serialization process is:
 * 1. Convert T to ByteArray using the provided [toByteArray] function
 * 2. Encode the ByteArray to a Base64 URL-safe string without padding
 * 3. Serialize the string to JSON
 *
 * The deserialization process is:
 * 1. Deserialize the JSON to a string
 * 2. Decode the Base64 URL-safe string without padding to a ByteArray
 * 3. Convert the ByteArray to T using the provided [fromByteArray] function
 *
 * @param T The type to be serialized/deserialized
 * @param serialName The name to be used in the serial descriptor
 * @param toByteArray A function that converts from T to ByteArray
 * @param fromByteArray A function that converts from ByteArray to T
 */
internal open class Base64UrlNoPaddingSerializer<T>(
    private val serialName: String,
    private val toByteArray: (T) -> ByteArray,
    private val fromByteArray: (ByteArray) -> T,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        val bytes = toByteArray(value)
        val base64String = Base64UrlNoPadding.encode(bytes)
        encoder.encodeString(base64String)
    }

    override fun deserialize(decoder: Decoder): T {
        val base64String = decoder.decodeString()
        val bytes =
            try {
                Base64UrlNoPadding.decode(base64String)
            } catch (e: IllegalArgumentException) {
                throw SerializationException("Failed to decode Base64UrlNoPadding", e)
            }
        return fromByteArray(bytes)
    }
}

internal object ByteArrayAsBase64UrlNoPaddingSerializer : Base64UrlNoPaddingSerializer<ByteArray>(
    "eu.europa.ec.eudi.statium.misc.ByteArrayBase64UrlNoPaddingSerializer",
    toByteArray = { it },
    fromByteArray = { it },
)
