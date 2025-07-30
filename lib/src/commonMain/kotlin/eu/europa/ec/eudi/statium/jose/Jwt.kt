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
package eu.europa.ec.eudi.statium.jose

import eu.europa.ec.eudi.statium.misc.Base64UrlNoPadding
import eu.europa.ec.eudi.statium.misc.StatiumJson
import eu.europa.ec.eudi.statium.misc.resultOf
import kotlinx.io.Buffer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource

internal inline fun <reified H, reified P> jwtHeaderAndPayload(jwt: String): Result<Pair<H, P>> =
    resultOf {
        val (headerPart, payloadPart, _) = parts(jwt)
        with(StatiumJson) {
            val header = decodeBase64<H>(headerPart)
            val statusList = decodeBase64<P>(payloadPart)
            header to statusList
        }
    }

private fun parts(jwt: String): Triple<String, String, String> {
    val parts = jwt.split(".")
    require(parts.size == 3) { "JWT string string must contain 3 parts" }
    val (header, payload, signature) = parts
    return Triple(header, payload, signature)
}

private inline fun <reified T> Json.decodeBase64(s: String): T {
    val decodedBytes = Base64UrlNoPadding.decode(s)
    return Buffer().use { buffer ->
        buffer.write(decodedBytes)
        decodeFromSource(buffer)
    }
}
