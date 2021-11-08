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

@file:OptIn(ExperimentalSerializationApi::class)

package com.gabrielleeg1.javarock.api.protocol.serialization

import com.gabrielleeg1.javarock.api.protocol.ProtocolEnum
import com.gabrielleeg1.javarock.api.protocol.writeString
import com.gabrielleeg1.javarock.api.protocol.writeVarInt
import io.ktor.utils.io.core.BytePacketBuilder
import io.ktor.utils.io.core.writeDouble
import io.ktor.utils.io.core.writeFloat
import io.ktor.utils.io.core.writeInt
import io.ktor.utils.io.core.writeLong
import io.ktor.utils.io.core.writeShort
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder

interface ProtocolEncoder : Encoder, CompositeEncoder

fun Encoder.asProtocolEncoder(): ProtocolEncoder {
  return this as? ProtocolEncoder
    ?: error("This serializer can be used only with Protocol format. Expected Encoder to be ProtocolEncoder, got ${this::class}")
}

internal class ProtocolEncoderImpl(
  val builder: BytePacketBuilder,
  val configuration: ProtocolConfiguration,
) : ProtocolEncoder {
  override val serializersModule = configuration.serializersModule

  override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

  override fun encodeBoolean(value: Boolean): Unit = builder.writeByte(if (value) 1 else 0)
  override fun encodeByte(value: Byte): Unit = builder.writeByte(value)
  override fun encodeChar(value: Char): Unit = builder.writeByte(value.code.toByte())
  override fun encodeDouble(value: Double): Unit = builder.writeDouble(value)
  override fun encodeFloat(value: Float): Unit = builder.writeFloat(value)
  override fun encodeInt(value: Int): Unit = builder.writeInt(value)
  override fun encodeLong(value: Long): Unit = builder.writeLong(value)
  override fun encodeShort(value: Short): Unit = builder.writeShort(value)
  override fun encodeString(value: String): Unit = builder.writeString(value)

  override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
    if (enumDescriptor.annotations.filterIsInstance<ProtocolEnum>().isNotEmpty()) {
      val value = enumDescriptor
        .getElementAnnotations(index)
        .filterIsInstance<ProtocolEnum.Entry>()
        .singleOrNull()
        ?.value
        ?: error("Can not encode enum ${enumDescriptor.serialName} index: 0 cause it does not have the @ProtocolEnum.Entry annotation")

      builder.writeVarInt(value)
    } else {
      builder.writeString(enumDescriptor.getElementName(index))
    }
  }

  @ExperimentalSerializationApi
  override fun encodeInline(inlineDescriptor: SerialDescriptor): Encoder = this

  override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
    encodeBoolean(value)
  }

  override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
    encodeByte(value)
  }

  override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
    encodeChar(value)
  }

  override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
    encodeDouble(value)
  }

  override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
    encodeFloat(value)
  }

  override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
    encodeInt(value)
  }

  override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
    encodeLong(value)
  }

  override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
    encodeShort(value)
  }

  override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
    encodeString(value)
  }

  @ExperimentalSerializationApi
  override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
    return encodeInline(descriptor)
  }

  @ExperimentalSerializationApi
  override fun <T : Any> encodeNullableSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T?
  ) {
    error("Can not encode null in Minecraft Protocol format.")
  }

  override fun <T> encodeSerializableElement(
    descriptor: SerialDescriptor,
    index: Int,
    serializer: SerializationStrategy<T>,
    value: T
  ) {
    serializer.serialize(this, value)
  }

  @ExperimentalSerializationApi
  override fun encodeNull(): Unit = error("Can not encode null in Minecraft Protocol format.")

  override fun endStructure(descriptor: SerialDescriptor) {
  }
}