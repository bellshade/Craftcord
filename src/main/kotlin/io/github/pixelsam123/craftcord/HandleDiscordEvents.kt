package io.github.pixelsam123.craftcord

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.Job
import org.bukkit.Bukkit

/**
 * Returns a list of handles to jobs that HAVE to be cancelled before plugin shutdown
 */
fun handleDiscordEvents(kord: Kord, textChannels: List<TextChannel>): List<Job> {

    val messageListenerJob = kord.on<MessageCreateEvent> {
        val channel = textChannels.find { textChannel -> textChannel.id == message.channelId }

        if (channel == null) {
            return@on
        }

        if (message.author != null && message.author?.id != kord.selfId) {
            val replyTarget = message.referencedMessage?.getAuthorAsMember()?.effectiveName

            val baseMessage =
                "[${channel.name}]${if (replyTarget == null) "" else " (replies to $replyTarget)"} <${message.getAuthorAsMember().effectiveName}> ${message.content}"

            if (message.attachments.isEmpty()) {
                Bukkit.broadcastMessage(baseMessage)
            } else {
                Bukkit.broadcastMessage(
                    "$baseMessage${if (message.content.isEmpty()) "" else " "}(${message.attachments.first().contentType ?: "unknown"} attached)"
                )
            }
        }
    }

    val commandListenerJob = kord.on<GuildChatInputCommandInteractionCreateEvent> {
        val response = interaction.deferPublicResponse()

        val players = Bukkit.getOnlinePlayers()

        response.respond {
            content = "Online players are: ${players.joinToString(", ") { player -> player.name }}"
        }
    }

    return listOf(messageListenerJob, commandListenerJob)
}
