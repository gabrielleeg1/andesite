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

package andesite.protocol.serialization

import andesite.protocol.extractPacketId
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import net.benwoodworth.knbt.Nbt

/**
 * Minecraft's protocol codec.
 *
 * This codec is used to serialize and deserialize objects to and from the Minecraft protocol.
 *
 * Example:
 * ```kotlin
 * @ProtocolPacket(0x00)
 * @Serializable
 * data class HandshakePacket(
 *   val protocolVersion: VarInt,
 *   val serverAddress: String,
 *   val serverPort: UShort,
 *   val nextState: NextState,
 * )
 *
 * @ProtocolEnum
 * @ProtocolVariant(Variant.VarInt)
 * @Serializable
 * enum class NextState {
 *   @ProtocolValue(1)
 *   Status,
 *
 *   @ProtocolValue(2)
 *   Login;
 * }
 *
 * val codec = MinecraftCodec { protocolVersion = 756 }
 * val packet = codec.decodeFromByteArray<HandshakePacket>(bytes)
 *
 * println(packet)
 * ```
 */
public class MinecraftCodec(public val configuration: ProtocolConfiguration) : BinaryFormat {
  override val serializersModule: SerializersModule = configuration.serializersModule

  /**
   * Decodes a packet from a byte array.
   *
   * @param deserializer The packet deserializer.
   * @param bytes The byte array to decode.
   * @return The decoded packet.
   */
  override fun <T> decodeFromByteArray(
    deserializer: DeserializationStrategy<T>,
    bytes: ByteArray
  ): T {
    return ProtocolDecoderImpl(ByteReadPacket(bytes), configuration)
      .decodeSerializableValue(deserializer)
  }

  /**
   * Encodes a packet to a byte array.
   *
   * @param serializer The packet serializer.
   * @param value The packet to encode.
   * @return The encoded packet.
   */
  override fun <T> encodeToByteArray(serializer: SerializationStrategy<T>, value: T): ByteArray {
    return buildPacket {
      ProtocolEncoderImpl(this, configuration).encodeSerializableValue(serializer, value)
    }.readBytes()
  }

  public companion object Versions
}

public typealias CodecBuilder = MinecraftCodecBuilder.() -> Unit

public val DefaultProtocolConfiguration: ProtocolConfiguration =
  ProtocolConfiguration(protocolVersion = -1)

public fun MinecraftCodec(
  from: ProtocolConfiguration = DefaultProtocolConfiguration,
  builder: CodecBuilder,
): MinecraftCodec {
  return MinecraftCodecBuilder(from).apply(builder).build()
}

public class RegistryBuilder(@PublishedApi internal val serializersModule: SerializersModule) {
  @PublishedApi
  internal val value: MutableMap<Int, KType> = mutableMapOf()

  public fun <A : Any> register(id: Int, type: KType) {
    value[id] = type
  }

  public inline fun <reified A : Any> register() {
    val serializer = serializersModule.serializer(typeOf<A>())
    value[extractPacketId(serializer.descriptor)] = typeOf<A>()
  }
}

public class MinecraftCodecBuilder(configuration: ProtocolConfiguration) {
  public var protocolVersion: Int = configuration.protocolVersion
  public var protocolVariant: ProtocolVariant = configuration.protocolVariant
  public var json: Json = configuration.json
  public var nbt: Nbt = configuration.nbt
  public var serializersModule: SerializersModule = configuration.serializersModule
  public var encryption: Boolean = configuration.encryption
  public var encodeDefaults: Boolean = configuration.encodeDefaults
  public var packetRegistry: Map<Int, KType> = configuration.packetRegistry

  public fun createPacketRegistry(builder: RegistryBuilder.() -> Unit): Map<Int, KType> {
    val registry = RegistryBuilder(serializersModule).apply(builder)

    return registry.value
  }

  internal fun build(): MinecraftCodec {
    require(protocolVersion != -1) { "protocolVersion must be set" }

    return MinecraftCodec(
      ProtocolConfiguration(
        protocolVersion,
        protocolVariant,
        serializersModule,
        packetRegistry,
        nbt,
        json,
        encryption,
        encodeDefaults,
      ),
    )
  }
}
