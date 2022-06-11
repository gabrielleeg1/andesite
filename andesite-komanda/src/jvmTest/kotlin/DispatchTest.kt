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

package andesite.komanda

import andesite.komanda.parsing.ExecutionNode
import andesite.protocol.misc.Chat
import andesite.protocol.misc.mordant
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class DispatchTest {
  @Test
  fun `test dispatch`(): Unit = runBlocking {
    val root = TestKomandaRoot()

    root.command("hello") {
      rootPattern {
        val target: Argument<String> by arguments
          .creating<String>()
          .suggests {
            add(Suggestion.empty())
          }
          .executes { value ->
            "test $value"
          }

        onExecution<String> {
          sendMessage("Hello, ${target.value()}!")
        }
      }
    }

    root.dispatch("hello world carlos", "Gabi")
//    root.dispatch("hello 'world'", "Gabi")
//    root.dispatch("hello target:'world'", "Gabi")
//    root.dispatch("hello target='world'", "Gabi")
//    root.dispatch("hello target= 'world'", "Gabi")
  }
}

class TestExecutionScope<S : Any>(override val sender: S, nodes: List<ExecutionNode>) :
  ExecutionScope<S> {
  override val arguments: Arguments = Arguments(nodes)

  override suspend fun sendMessage(chat: Chat) {
    println("message: ${chat.mordant()}")
  }

  override suspend fun failwith(chat: Chat) {
    println("fail: ${chat.mordant()}")
  }
}

class TestKomandaRoot : AbstractKomandaRoot<String>() {
  override fun createExecutionScope(sender: String, nodes: List<ExecutionNode>) =
    TestExecutionScope(sender, nodes)
}
