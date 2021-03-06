/*
 *    Copyright 2022 Gabrielle Guimarães de Oliveira
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

package andesite.protocol.misc

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public typealias Uuid = @Serializable(UuidSerializer::class) com.benasher44.uuid.Uuid

public object UuidSerializer : KSerializer<Uuid> {
  override val descriptor: SerialDescriptor =
    buildClassSerialDescriptor("Uuid") {
      element<Long>("mostSignificantBits")
      element<Long>("leastSignificantBits")
    }

  override fun serialize(encoder: Encoder, value: Uuid) {
    encoder.encodeLong(value.mostSignificantBits)
    encoder.encodeLong(value.leastSignificantBits)
  }

  override fun deserialize(decoder: Decoder): Uuid {
    return Uuid(decoder.decodeLong(), decoder.decodeLong())
  }
}
