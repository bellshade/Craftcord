package io.github.pixelsam123.craftcord

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.exception.KordInitializationException
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

class Craftcord : JavaPlugin() {

    private var kord: Kord? = null

    override fun onEnable() {
        val placeholderToken = "yourDiscordBotTokenHere"

        logger.info("Enabling Craftcord!")

        saveDefaultConfig()

        val token = config.getString("token") ?: placeholderToken

        if (token == placeholderToken) {
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

        val kord = kord ?: return

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

        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                kord.createGuildChatInputCommand(channel.guildId, "list", "List online players")
            }
        }

        val config = PluginConfig(
            textChannels = textChannels,
        )

        handleDiscordEvents(kord, textChannels)
        server.pluginManager.registerEvents(MinecraftEventsListener(this, config), this)
        getCommand("craftcord")?.setExecutor(MinecraftCommandsHandler(config))

        logger.info("Craftcord successfully enabled!")
    }

    override fun onDisable() {
        logger.info("Disabling Craftcord!")

        runBlocking {
            kord?.shutdown()
        }

        logger.info("Craftcord successfully disabled!")
    }

    fun setBotStatus(state: String) {
        val kord = kord ?: return

        launchJob {
            kord.editPresence { this.state = state }
        }
    }

}
