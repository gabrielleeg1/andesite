/*
 *    Copyright 2021 Gabrielle Guimarães de Oliveira
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package andesite.protocol.java.handshake

import andesite.protocol.ProtocolJson
import andesite.protocol.ProtocolPacket
import andesite.protocol.java.JavaPacket
import andesite.protocol.misc.Chat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Response(
  val version: Version,
  val players: Players,
  val description: Chat,
  val favicon: String? = null,
)

@Serializable
public data class Version(
  val name: String,
  val protocol: Int,
)

@Serializable
public data class Players(
  val max: Int,
  val online: Int,
  val sample: List<Sample> = emptyList(),
)

@Serializable
public data class Sample(val name: String, val id: String)

@Serializable
@SerialName("ResponsePacket")
@ProtocolPacket(0x00)
public data class ResponsePacket(@ProtocolJson val response: Response) : JavaPacket
