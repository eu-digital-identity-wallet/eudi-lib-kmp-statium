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

import kotlin.io.encoding.Base64

internal object Base64UrlNoPadding {

    private fun base64UrlNoPadding() = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

    fun encode(source: ByteArray, startIndex: Int = 0, endIndex: Int = source.size): String =
        base64UrlNoPadding().encode(source, startIndex, endIndex)

    fun decode(source: CharSequence): ByteArray = base64UrlNoPadding().decode(source)
}
