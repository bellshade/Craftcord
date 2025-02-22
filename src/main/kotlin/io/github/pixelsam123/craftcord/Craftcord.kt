package io.github.pixelsam123.craftcord

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

        if (kord !== null) {
            val kord = kord!!
            val textChannels = config.getLongList("textChannels")

            CoroutineScope(Dispatchers.IO).launch {
                kord.login {
                    @OptIn(PrivilegedIntent::class)
                    intents += Intent.MessageContent
                }
            }

            kord.on<MessageCreateEvent> {
                if (message.channelId.value.toLong() in textChannels) {
                    val channel = kord.getChannelOf<TextChannel>(message.channelId)

                    if (channel === null) {
                        logger.warning(
                            "There is an invalid Discord text channel ID, this message will not be sent to Minecraft."
                        )
                    } else if (message.author?.id != kord.selfId) {
                        if (message.attachments.isNotEmpty()) {
                            Bukkit.broadcastMessage(
                                "[${channel.name}] <${message.getAuthorAsMember().effectiveName}> ${message.content} (${message.attachments.first().contentType ?: "unknown"} attached)"
                            )
                        } else {
                            Bukkit.broadcastMessage(
                                "[${channel.name}] <${message.getAuthorAsMember().effectiveName}> ${message.content}"
                            )
                        }
                    }
                }
            }

            server.pluginManager.registerEvents(ChatListener(textChannels, kord), this)

            logger.info("Craftcord successfully enabled!")
        }
    }

    override fun onDisable() {
        logger.info("Disabling Craftcord!")

        runBlocking {
            kord?.shutdown()
        }

        logger.info("Craftcord successfully disabled!")
    }

}
