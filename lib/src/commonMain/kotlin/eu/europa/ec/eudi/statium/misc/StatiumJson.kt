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

import eu.europa.ec.eudi.statium.CompressedByteArray
import eu.europa.ec.eudi.statium.DurationAsSeconds
import eu.europa.ec.eudi.statium.InstantAsEpocSeconds
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * [SerializersModule] for JSON format. Defines that
 * - A [CompressedByteArray] is serialized via [ByteArrayAsBase64UrlNoPaddingSerializer]
 */
public val StatiumJsonSerializersModule: SerializersModule =
    SerializersModule {
        contextual(CompressedByteArray::class, ByteArrayAsBase64UrlNoPaddingSerializer)
        contextual(InstantAsEpocSeconds::class, EpocSecondsSerializer)
        contextual(DurationAsSeconds::class, DurationAsSecondsSerializer)
    }

public val StatiumJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
        explicitNulls = false
        prettyPrint = false
        serializersModule = StatiumJsonSerializersModule
    }
