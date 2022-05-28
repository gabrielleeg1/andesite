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

package andesite.server.java.server

import andesite.protocol.misc.Chat
import andesite.protocol.serialization.MinecraftCodec
import andesite.server.GameServer
import andesite.server.GameServerBuilder
import andesite.server.Motd
import andesite.server.MotdBuilder
import andesite.world.Location
import andesite.world.anvil.block.BlockRegistry
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.CoroutineScope

fun createJavaServer(scope: CoroutineScope, builder: GameServerBuilder.() -> Unit): GameServer {
  return GameServerBuilderImpl(scope).apply(builder).build()
}

private class MotdBuilderImpl : MotdBuilder {
  override var version: String by BuilderProperty()
  override var maxPlayers: Int by BuilderProperty()
  override var text: Chat by BuilderProperty()

  fun build(): Motd {
    return Motd(version, maxPlayers, text)
  }
}

private class GameServerBuilderImpl(val scope: CoroutineScope) : GameServerBuilder {
  override var hostname: String by BuilderProperty()
  override var port: Int by BuilderProperty()
  override var spawn: Location by BuilderProperty()
  override var blockRegistry: BlockRegistry by BuilderProperty()
  override var codec: MinecraftCodec by BuilderProperty()
  var motd: Motd by BuilderProperty()

  override fun motd(builder: MotdBuilder.() -> Unit) {
    motd = MotdBuilderImpl().apply(builder).build()
  }

  fun build(): GameServer {
    return JavaGameServer(scope, hostname, port, spawn, motd, codec, blockRegistry)
  }
}

private class BuilderProperty<T : Any> : ReadWriteProperty<Any?, T> {
  private var value: T? = null

  override fun getValue(thisRef: Any?, property: KProperty<*>): T {
    return requireNotNull(value) { "Property ${property.name} should be initialized before build." }
  }

  override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this.value = value
  }
}
