package io.github.pixelsam123.craftcord

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import kotlinx.coroutines.Job
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
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

        if (interaction.invokedCommandName == "list") {
            val players = Bukkit.getOnlinePlayers()
            response.respond {
                content = "Online players are: ${players.joinToString(", ") { player -> player.name }}"
            }
        } else if (interaction.invokedCommandName == "tps") {
            try {
                val spark = SparkProvider.get()
                val tps = spark.tps()!!

                val tps5Sec = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5)
                val tps1Min = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1)
                val tps5Min = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_5)
                val tps15Min = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15)

                response.respond {
                    content = """
                        ```r
                        5 seconds    : ${"%.2f".format(tps5Sec)} TPS
                        1 minute     : ${"%.2f".format(tps1Min)} TPS
                        5 minutes    : ${"%.2f".format(tps5Min)} TPS
                        15 minutes   : ${"%.2f".format(tps15Min)} TPS
                        ```
                    """.trimIndent()
                }
            } catch (_: IllegalStateException) {
                response.respond {
                    content = "Spark is not installed on this server."
                }
            }
        }
    }

    return listOf(messageListenerJob, commandListenerJob)
}
