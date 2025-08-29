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

import eu.europa.ec.eudi.statium.GetStatusListTokenUsingCwt
import eu.europa.ec.eudi.statium.StatusListTokenClaims
import eu.europa.ec.eudi.statium.TokenStatusListSpec
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ParseCwtHeaderAndPayloadTest {

    private val parseCwt =
        ParseCwt.mapping<GetStatusListTokenUsingCwt.ProtectedHeader, StatusListTokenClaims>()

    @Test
    fun testCwtUsingCoseSign1() = runTest {
        val input = """
            d2845820a2012610781a6170706c69636174696f6e2f7374617475736c6973742b63
            7774a1044231325850a502782168747470733a2f2f6578616d706c652e636f6d2f73
            74617475736c697374732f31061a648c5bea041a8898dfea19fffe19a8c019fffda2
            646269747301636c73744a78dadbb918000217015d584027d5535dfe0a33291cc9bf
            b41053ad2493c49d1ee4635e12548a79bac92916845fee76799c42762f928441c5c3
            44e3612381e0cf88f2f160b3e1f97728ec8403
        """.trimIndent().replace("\n", "").hexToByteArray()

        val (protectedHeader, statusListTokenClaims) = parseCwt(input)

        assertEquals(
            expected = GetStatusListTokenUsingCwt.ProtectedHeader(TokenStatusListSpec.MEDIA_TYPE_APPLICATION_STATUS_LIST_CWT),
            actual = protectedHeader,
        )

        println(protectedHeader)
        println(statusListTokenClaims)
    }
}
