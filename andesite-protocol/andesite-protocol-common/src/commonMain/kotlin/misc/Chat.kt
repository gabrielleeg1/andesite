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

package andesite.protocol.misc

import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Chat")
public data class Chat(
  public val text: String,
  public val bold: Boolean = false,
  public val italic: Boolean = false,
  public val underlined: Boolean = false,
  public val strikethrough: Boolean = false,
  public val obfuscated: Boolean = false,
  public val font: Identifier = Identifier("minecraft:default"),
  public val color: Color = Color.White,
  public val insertion: String? = null,
  public val clickEvent: ClickEvent? = null,
  public val hoverEvent: HoverEvent? = null,
  public val extra: List<Chat>? = null,
) {
  public companion object {
    public const val ColorCode: String = "\u00A7"

    /**
     * Gets a [Chat] object from a string converting color codes
     *
     * TODO: convert color codes
     */
    public fun of(text: String): Chat {
      return Chat(text.replace("&", ColorCode))
    }

    public fun build(text: String, builder: ChatBuilder.() -> Unit): Chat {
      return ChatBuilder(of(text)).apply(builder).build()
    }

    public fun build(chat: Chat, builder: ChatBuilder.() -> Unit): Chat {
      return ChatBuilder(chat).apply(builder).build()
    }

    public fun many(builder: ChatListBuilder.() -> Unit): List<Chat> {
      return ChatListBuilder().apply(builder).build()
    }
  }

  public fun with(chats: Collection<Chat>): Chat {
    return when {
      chats.isEmpty() -> this
      else -> copy(extra = extra.orEmpty().plus(chats))
    }
  }

  public fun hoverEvent(hoverEvent: HoverEvent): Chat {
    return copy(hoverEvent = hoverEvent)
  }

  public fun clickEvent(clickEvent: ClickEvent): Chat {
    return copy(clickEvent = clickEvent)
  }

  public operator fun plus(other: Chat): Chat {
    return copy(extra = extra.orEmpty().plusElement(other))
  }

  public operator fun plus(other: String): Chat {
    return copy(text = text + other)
  }
}

public typealias PlaceholderProvider =
  PropertyDelegateProvider<Nothing?, ReadOnlyProperty<Nothing?, Chat>>

public class ChatListBuilder internal constructor() {
  private val components: MutableList<Chat> = mutableListOf()

  public fun append(chat: Chat) {
    components += chat
  }

  public fun append(text: String, builder: ChatBuilder.() -> Unit) {
    components += Chat.build(text, builder)
  }

  public fun build(): List<Chat> = components
}

public class ChatBuilder internal constructor(private val initial: Chat) {
  private val components: MutableList<Chat> = mutableListOf()
  private val placeholders: MutableMap<String, Chat> = mutableMapOf()

  public var clickEvent: ClickEvent? = initial.clickEvent
  public var hoverEvent: HoverEvent? = initial.hoverEvent
  public var insertion: String? = initial.insertion
  public var font: Identifier = initial.font
  public var color: Color = initial.color
  public var italic: Boolean = initial.italic
  public var strikethrough: Boolean = initial.strikethrough
  public var underlined: Boolean = initial.underlined
  public var obfuscated: Boolean = initial.obfuscated
  public var bold: Boolean = initial.bold

  public fun append(text: String, builder: ChatBuilder.() -> Unit = {}) {
    components += Chat.build(text, builder)
  }

  public fun append(chat: Chat, builder: ChatBuilder.() -> Unit = {}) {
    components += ChatBuilder(chat).apply(builder).build()
  }

  public fun placeholder(key: String, chat: Chat): Chat {
    placeholders[key] = chat
    return chat
  }

  public fun placeholder(key: String, text: String, builder: ChatBuilder.() -> Unit = {}): Chat {
    val chat = Chat.build(text, builder)
    placeholders[key] = chat
    return chat
  }

  public fun placeholder(text: String, builder: ChatBuilder.() -> Unit): PlaceholderProvider =
    PropertyDelegateProvider { _, property ->
      val chat = Chat.build(text, builder)

      placeholders[property.name] = chat

      ReadOnlyProperty { _, _ -> chat }
    }

  public fun hex(hex: String) {
    color = HexColor(hex)
  }

  public fun italic() {
    italic = true
  }

  public fun strikethrough() {
    strikethrough = true
  }

  public fun underlined() {
    underlined = true
  }

  public fun obfuscated() {
    obfuscated = true
  }

  public fun bold() {
    bold = true
  }

  public fun black() {
    color = Color.Black
  }

  public fun darkBlue() {
    color = Color.DarkBlue
  }

  public fun darkGreen() {
    color = Color.DarkGreen
  }

  public fun darkCyan() {
    color = Color.DarkCyan
  }

  public fun darkRed() {
    color = Color.DarkRed
  }

  public fun purple() {
    color = Color.Purple
  }

  public fun gold() {
    color = Color.Gold
  }

  public fun gray() {
    color = Color.Gray
  }

  public fun darkGray() {
    color = Color.DarkGray
  }

  public fun blue() {
    color = Color.Blue
  }

  public fun brightGreen() {
    color = Color.BrightGreen
  }

  public fun cyan() {
    color = Color.Cyan
  }

  public fun red() {
    color = Color.Red
  }

  public fun pink() {
    color = Color.Pink
  }

  public fun yellow() {
    color = Color.Yellow
  }

  public fun white() {
    color = Color.White
  }

  public fun build(): Chat {
    val chat = initial.copy(
      hoverEvent = hoverEvent,
      clickEvent = clickEvent,
      font = font,
      color = color,
      italic = italic,
      bold = bold,
      strikethrough = strikethrough,
      underlined = underlined,
      obfuscated = obfuscated,
      insertion = insertion,
    )

    val texts = quoteString(chat.text).map { quote ->
      when (quote.placeholder) {
        true -> placeholders[quote.text] ?: chat.copy(text = quote.fullText, extra = null)
        false -> chat.copy(text = quote.fullText, extra = null)
      }
    }

    return chat.copy(text = "").with(texts).with(components)
  }
}
