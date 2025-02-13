package io.github.pixelsam123.craftcord

import dev.kord.core.Kord
import dev.kord.core.exception.KordInitializationException
import kotlinx.coroutines.runBlocking
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
            server.pluginManager.registerEvents(ChatListener(config, kord!!), this)
            logger.info("Craftcord successfully enabled!")
        }
    }

    override fun onDisable() {
        logger.info("Disabling Craftcord!")

        runBlocking {
            kord?.logout()
        }

        logger.info("Craftcord successfully disabled!")
    }

}
