package io.github.pixelsam123.craftcord

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.exception.KordInitializationException
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Craftcord : JavaPlugin() {

    private var kord: Kord? = null

    override fun onEnable() {
        logger.info("Enabling Craftcord!")

        saveDefaultConfig()

        val token = config.getString("token") ?: "yourDiscordBotTokenHere"

        if (token.isBlank()) {
            logger.warning(
                "Your token is still the default placeholder token, please specify a token in config.yml"
            )
            logger.warning("This plugin will silently fail with the placeholder token.")
        } else {
            runBlocking {
                try {
                    kord = Kord(token)
                } catch (err: KordInitializationException) {
                    logger.warning("Error connecting to Discord. Here is the error message:")
                    logger.warning(err.message)
                    logger.warning("This plugin will now silently fail.")
                }
            }
        }

        if (kord == null) {
            return
        }
        val kord = kord!!

        val textChannelIds = config.getLongList("textChannels")

        CoroutineScope(Dispatchers.IO).launch {
            kord.login {
                @OptIn(PrivilegedIntent::class)
                intents += Intent.MessageContent
            }
        }

        val textChannels = runBlocking {
            val potentiallyNullChannels = textChannelIds.map { channelId ->
                kord.getChannelOf<TextChannel>(
                    Snowflake(channelId)
                )
            }

            potentiallyNullChannels.forEachIndexed { index, channel ->
                if (channel == null) {
                    logger.warning(
                        "There is an invalid Discord text channel ID (index $index in config), this channel will not be used."
                    )
                }
            }

            potentiallyNullChannels.filterNotNull()
        }

        kord.on<MessageCreateEvent> {
            // TODO: Move this to a separate function
            if (message.channelId.value.toLong() in textChannelIds) {
                val channel =
                    textChannels.find { textChannel -> textChannel.id == message.channelId }

                if (channel == null) {
                    logger.warning(
                        "Error getting the text channel of this message, this message will not be sent to Minecraft."
                    )
                } else if (message.author?.id != kord.selfId) {
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
        }

        server.pluginManager.registerEvents(MessageableListener(textChannels), this)

        logger.info("Craftcord successfully enabled!")
    }

    override fun onDisable() {
        logger.info("Disabling Craftcord!")

        runBlocking {
            kord?.shutdown()
        }

        logger.info("Craftcord successfully disabled!")
    }

}
