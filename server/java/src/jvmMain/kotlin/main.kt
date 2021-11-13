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

package com.gabrielleeg1.javarock.server.java

import com.gabrielleeg1.javarock.api.protocol.java.handshake.HandshakePacket
import com.gabrielleeg1.javarock.api.protocol.java.handshake.NextState
import com.gabrielleeg1.javarock.api.protocol.resource
import com.gabrielleeg1.javarock.api.protocol.serialization.MinecraftCodec
import com.gabrielleeg1.javarock.api.protocol.serializers.UuidSerializer
import com.gabrielleeg1.javarock.api.world.anvil.readAnvilWorld
import com.gabrielleeg1.javarock.server.java.player.Session
import com.gabrielleeg1.javarock.server.java.player.receivePacket
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import mu.KotlinLogging
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtVariant
import java.io.File
import java.util.concurrent.Executors

private val logger = KotlinLogging.logger("javarock")

private val context = Executors.newCachedThreadPool().asCoroutineDispatcher()

internal val nbt = Nbt {
  variant = NbtVariant.Java
  compression = NbtCompression.None
  ignoreUnknownKeys = true
}

internal val world = readAnvilWorld(File(resource("world")))

suspend fun main(): Unit = withContext(context) {
  val selector = ActorSelectorManager(Dispatchers.IO)
  val server = aSocket(selector).tcp().bind(hostname = "0.0.0.0", port = 25565)
  val codec = MinecraftCodec {
    protocolVersion = 756
    json = Json {
      prettyPrint = true
    }
    serializersModule = SerializersModule {
      contextual(UuidSerializer)
    }
  }

  logger.info { "Server started at 0.0.0.0:25565" }

  while (true) {
    val session = Session(codec, server.accept())

    launch {
      try {
        val handshake = session.receivePacket<HandshakePacket>()

        when (handshake.nextState) {
          NextState.Status -> handleStatus(session, handshake)
          NextState.Login -> handlePlay(session, handleLogin(session, handshake))
        }
      } catch (error: Throwable) {
        logger.error(error) {
          "Error thrown while handling connection ${session.socket.remoteAddress}"
        }

        withContext(Dispatchers.IO) {
          session.socket.close()
        }
      }
    }
  }
}
