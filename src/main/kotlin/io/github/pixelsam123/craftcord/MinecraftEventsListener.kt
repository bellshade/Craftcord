package io.github.pixelsam123.craftcord

import dev.kord.core.behavior.channel.createWebhook
import kotlinx.coroutines.flow.first
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class MinecraftEventsListener(
    private val plugin: Craftcord,
    private val config: PluginConfig,
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val minecraftUsername = event.player.name
        val discordUsername = config.minecraftUsernameToDiscordUsername[minecraftUsername]

        if (discordUsername != null) {
            for (channel in config.textChannels) {
                launchJob {
                    val webhook = channel.createWebhook(discordUsername) {
                        name = discordUsername
                        avatar = channel.guild.members.first {
                            it.username == discordUsername
                        }.avatar?.getImage()
                    }

                    webhook.channel.createMessage(event.message)

                    webhook.delete("Automated Craftcord Delete")
                }
            }
        } else {
            for (channel in config.textChannels) {
                launchJob {
                    channel.createMessage("<${minecraftUsername}> ${event.message}")
                }
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerCount = Bukkit.getOnlinePlayers().size
        val playerWord = if (playerCount == 1) "player" else "players"

        for (channel in config.textChannels) {
            launchJob {
                channel.createMessage("${event.player.name} joined the server ($playerCount $playerWord online)")
            }
        }

        plugin.setBotStatus("$playerCount $playerWord online")
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        /**
         * PlayerQuitEvent is apparently supplied before the online players value decreases
         * so we decrease it manually
         */
        val playerCount = Bukkit.getOnlinePlayers().size - 1
        val playerWord = if (playerCount == 1) "player" else "players"

        for (channel in config.textChannels) {
            launchJob {
                channel.createMessage("${event.player.name} left the server ($playerCount $playerWord online)")
            }
        }

        plugin.setBotStatus("$playerCount $playerWord online")
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        for (channel in config.textChannels) {
            launchJob {
                channel.createMessage(event.deathMessage ?: "${event.entity.name} died")
            }
        }
    }

}
