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

import eu.europa.ec.eudi.statium.misc.StatiumCbor
import eu.europa.ec.eudi.statium.misc.StatiumJson
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromHexString
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusListFromRawBytesTest {

    @Test
    fun test1WithJson() = runTest {
        assertEquals(
            expected = StatiumJson.decodeFromString<StatusList>(
                """
            {
              "bits": 1,
              "lst": "eNrbuRgAAhcBXQ"
            }
                """.trimIndent(),
            ),
            actual = StatusList.fromRawBytes(
                bytesPerStatus = BitsPerStatus.One,
                rawList = byteArrayOf(0xb9.toByte(), 0xa3.toByte()),
            ),
        )
    }

    @Test
    fun test1WithCwt() = runTest {
        assertEquals(
            expected = StatiumCbor.decodeFromHexString<StatusList>("a2646269747301636c73744a78dadbb918000217015d"),
            actual = StatusList.fromRawBytes(
                bytesPerStatus = BitsPerStatus.One,
                rawList = byteArrayOf(0xb9.toByte(), 0xa3.toByte()),
            ),
        )
    }

    @Test
    fun test2WithJson() = runTest {
        assertEquals(
            expected = StatiumJson.decodeFromString<StatusList>(
                """
            {
              "bits": 2,
              "lst": "eNo76fITAAPfAgc"
            }
                """.trimIndent(),
            ),
            actual = StatusList.fromRawBytes(
                bytesPerStatus = BitsPerStatus.Two,
                rawList = byteArrayOf(0xc9.toByte(), 0x44.toByte(), 0xf9.toByte()),
            ),
        )
    }
}
