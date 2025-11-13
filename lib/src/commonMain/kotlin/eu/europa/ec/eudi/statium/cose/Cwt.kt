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

import eu.europa.ec.eudi.statium.cose.ParseCwt.CoseProtectedHeaderAndPayload
import eu.europa.ec.eudi.statium.misc.StatiumCbor
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.CborArray
import kotlinx.serialization.cbor.ObjectTags
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.serializer

public fun interface ParseCwt<out ProtectedHeader, out Payload> {

    public suspend operator fun invoke(input: ByteArray): CoseProtectedHeaderAndPayload<ProtectedHeader, Payload>

    public data class CoseProtectedHeaderAndPayload<out ProtectedHeader, out Payload>(
        public val protectedHeader: ProtectedHeader,
        public val payload: Payload?,
    )

    public companion object {
        internal fun <PH, P> map(
            protectedHeaderSerializer: kotlinx.serialization.KSerializer<PH>,
            payloadSerializer: kotlinx.serialization.KSerializer<P>,
            parseCwt: ParseCwt<ByteArray, ByteArray>,
        ): ParseCwt<PH, P> = ParseCwt { input ->
            val (protectedHeaderBytes, payloadBytes) = ParseCwtUsingKotlinx(input)
            with(StatiumCbor) {
                val protectedHeader = decodeFromByteArray(protectedHeaderSerializer, protectedHeaderBytes)
                val payload = payloadBytes?.let { decodeFromByteArray(payloadSerializer, it) }
                CoseProtectedHeaderAndPayload(protectedHeader, payload)
            }
        }

        internal inline fun <reified PH, reified P> map(
            parseCwt: ParseCwt<ByteArray, ByteArray>,
        ): ParseCwt<PH, P> {
            val serializersModule = StatiumCbor.serializersModule
            return map(serializersModule.serializer(), serializersModule.serializer(), parseCwt)
        }

        public fun default(): ParseCwt<ByteArray, ByteArray> = ParseCwtUsingKotlinx

        internal inline fun <reified PH, reified P> mapping(): ParseCwt<PH, P> = map(default())
    }
}

internal object ParseCwtUsingKotlinx : ParseCwt<ByteArray, ByteArray> {

    override suspend fun invoke(input: ByteArray): CoseProtectedHeaderAndPayload<ByteArray, ByteArray> {
        return parseCoseSign1(input)
    }

    private val parseCoseSign1: ParseCwt<ByteArray, ByteArray> = ParseCwt { input ->
        /**
         * In this context, we don't care about the protected header
         */
        @Serializable
        class CoseUnprotectedHeader()

        @Serializable
        @CborArray
        @ObjectTags(RFC8152.COSE_SIGN1_TAG)
        class CwtWithCoseSign1(
            val protectedHeader: ByteArray,
            val unprotectedHeader: CoseUnprotectedHeader,
            val payload: ByteArray,
            val signature: ByteArray,
        )
        val coseSign1 = StatiumCbor.decodeFromByteArray<CwtWithCoseSign1>(input)
        CoseProtectedHeaderAndPayload(coseSign1.protectedHeader, coseSign1.payload)
    }
}
