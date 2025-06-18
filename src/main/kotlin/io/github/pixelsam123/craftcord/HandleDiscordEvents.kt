package io.github.pixelsam123.craftcord

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.behavior.interaction.suggest
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.addFile
import kotlinx.coroutines.flow.firstOrNull
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import org.bukkit.Bukkit
import org.bukkit.HeightMap
import org.bukkit.Material
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import javax.imageio.ImageIO

fun handleDiscordEvents(craftcord: Craftcord, kord: Kord, textChannels: List<TextChannel>) {

    kord.on<MessageCreateEvent> {
        val channel = textChannels.find { textChannel -> textChannel.id == message.channelId }

        if (channel == null) {
            return@on
        }

        if (message.author != null && message.author?.id != kord.selfId) {
            val webhookId = message.referencedMessage?.webhookId
            val replyTarget = if (webhookId == null) message.referencedMessage?.getAuthorAsMember()?.effectiveName else {
                channel.webhooks.firstOrNull { it.id == webhookId }?.name
            }

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

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
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
        } else if (interaction.invokedCommandName == "locate") {
            val playerName = interaction.command.strings["player"]!!
            val player = craftcord.server.getPlayer(playerName)

            if (player == null) {
                response.respond {
                    content = "Player `$playerName` is not online."
                }

                return@on
            }

            val location = player.location
            val world = try {
                location.world
            } catch (_: IllegalArgumentException) {
                null
            }

            if (world == null) {
                response.respond {
                    content = "Player `$playerName` is in an unloaded world."
                }

                return@on
            }

            val img = BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB)

            val topLeftX = location.blockX - 128
            val topLeftZ = location.blockZ - 128

            for (x in 0 until 256) {
                var lastY: Int = Int.MIN_VALUE

                for (z in 0 until 256) {
                    val block = world.getHighestBlockAt(topLeftX + x, topLeftZ + z, HeightMap.WORLD_SURFACE)
                    var color = Color(block.blockData.mapColor.asRGB())

                    val y = if (block.type == Material.WATER) world.getHighestBlockAt(topLeftX + x, topLeftZ + z, HeightMap.OCEAN_FLOOR).y
                            else block.y

                    if (y < lastY) {
                        color = color.darker()
                    } else if (lastY != Int.MIN_VALUE && y > lastY) {
                        color = color.brighter()
                    }

                    lastY = y

                    // Each block fills 2x2 pixels
                    img.setRGB(2*x, 2*z, color.rgb)
                    img.setRGB(2*x + 1, 2*z, color.rgb)
                    img.setRGB(2*x, 2*z + 1, color.rgb)
                    img.setRGB(2*x + 1, 2*z + 1, color.rgb)
                }
            }

            // Write temporary map image to a file
            val fileName = "map${interaction.id}.png"
            val path = craftcord.dataFolder.resolve("temp/${fileName}").toPath()

            path.parent.toFile().mkdir()
            ImageIO.write(img, "png", path.toFile())

            response.respond {
                content = "Location: `${location.blockX}, ${location.blockY}, ${location.blockZ}` in world `${world.name}`"
                addFile(path)
            }

            // Clean up the temporary file
            Files.deleteIfExists(path)
        }
    }

    kord.on<AutoCompleteInteractionCreateEvent> {
        val cmdName = interaction.command.rootName

        if (cmdName == "locate") {
            interaction.suggestString {
                craftcord.server.onlinePlayers.forEach {
                    if (it.name.startsWith(interaction.focusedOption.value, ignoreCase = true)) {
                        choice(it.name, it.name)
                    }
                }
            }
        } else {
            interaction.suggest(emptyList())
        }
    }
}
