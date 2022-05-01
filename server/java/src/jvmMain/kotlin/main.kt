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

package com.gabrielleeg1.andesite.server.java

import io.klogging.Level
import io.klogging.config.loggingConfiguration
import io.klogging.rendering.RENDER_ANSI
import io.klogging.sending.STDOUT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import java.lang.System.getSecurityManager
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

suspend fun main() {
  loggingConfiguration {
    kloggingMinLevel(Level.INFO)
    sink("stdout", RENDER_ANSI, STDOUT)
    logging("Andesite", "com.gabrielleeg1") {
      fromMinLevel(Level.TRACE) {
        toSink("stdout")
      }
    }
    logging("Sink", "Dispatcher", "Configuration", "Emitter") {
      fromMinLevel(Level.ERROR) {
        toSink("stdout")
      }
    }
  }

  withContext(scope.coroutineContext) {
    startAndesite()
  }
}

private val context = Executors
  .newCachedThreadPool(AndesiteThreadFactory)
  .asCoroutineDispatcher()

private val scope = CoroutineScope(context)

private object AndesiteThreadFactory : ThreadFactory {
  const val NAME_PREFIX = "andesite-pool-"
  val threadNumber = AtomicInteger(0)
  val group: ThreadGroup = getSecurityManager()?.threadGroup ?: Thread.currentThread().threadGroup

  override fun newThread(runnable: Runnable): Thread {
    return Thread(group, runnable, NAME_PREFIX + threadNumber.incrementAndGet(), 0).apply {
      isDaemon = false
      priority = Thread.NORM_PRIORITY
    }
  }
}