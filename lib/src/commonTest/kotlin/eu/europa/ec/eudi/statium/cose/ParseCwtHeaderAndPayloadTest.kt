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

    @Test
    fun testWithAnotherCWT() = runTest {
        val input = """
               d284590308a3012610781a6170706c69636174696f6e2f7374617475736c6973742
               b63777418215902e3308202df30820285a00302010202147f7968853983300992fd
               85ffab886aa11c89079e300a06082a8648ce3d040302305c311e301c06035504030
               c1550494420497373756572204341202d205554203032312d302b060355040a0c24
               455544492057616c6c6574205265666572656e636520496d706c656d656e7461746
               96f6e310b3009060355040613025554301e170d3235303431303134333735325a17
               0d3236303730343134333735315a30523114301206035504030c0b5049442044532
               02d203031312d302b060355040a0c24455544492057616c6c657420526566657265
               6e636520496d706c656d656e746174696f6e310b300906035504061302555430593
               01306072a8648ce3d020106082a8648ce3d03010703420004bb580016a8fcded14b
               37cfca5a8f254f581466ad16c28b95f6b3d1af9726d0cadc13ba67199de8fd0642d
               f020965a17e6dbfe36059f0df82dff4eacfb9b55e25a382012d30820129301f0603
               551d2304183016801462c7944728bd0fa21620a79ac2499444f101d3c7301b06035
               51d110414301282106973737565722e65756469772e64657630160603551d250101
               ff040c300a06082b8102020000010230430603551d1f043c303a3038a036a034863
               268747470733a2f2f70726570726f642e706b692e65756469772e6465762f63726c
               2f7069645f43415f55545f30322e63726c301d0603551d0e04160414aa5fe8a7191
               0958cb4965693a0f6c313f9b211c1300e0603551d0f0101ff040403020780305d06
               03551d1204563054865268747470733a2f2f6769746875622e636f6d2f65752d646
               9676974616c2d6964656e746974792d77616c6c65742f6172636869746563747572
               652d616e642d7265666572656e63652d6672616d65776f726b300a06082a8648ce3
               d0403020348003045022100d255483b2a4f722419c2965a049eb9b90339d8b9fd41
               3d6f5185fd7e5f41115a022069e6dead1e1f17c0584fb2dcce1cca29bc10ff1b09a
               cd110148264a7ea4bbc1aa1044131589fa402786e68747470733a2f2f6465762e69
               73737565722e65756469772e6465762f746f6b656e5f7374617475735f6c6973742
               f46432f65752e6575726f70612e65632e657564692e7069642e312f353237646438
               37642d616365622d343435392d613862332d646537376335386637613166061a68b
               ac8ef19fffe190e1019fffda2646269747301636c73745278da63601805a360148c
               82e10a0004e200015847304502206a1151e0bfe5fd4ef162966f3c3c2f0ecb454d2
               e1598b3117d6415941ff671b5022100a6b38afbfe7614e106466dbf4cd117da5dc6
               a78e306f521f3262557adfcc006e
        """.trimIndent().replace("\n", "").hexToByteArray()

        val (protectedHeader, statusListTokenClaims) = parseCwt(input)

        println(protectedHeader)
        println(statusListTokenClaims)
    }
}
