package io.github.pixelsam123.craftcord

import dev.kord.common.annotation.KordExperimental
import dev.kord.core.behavior.channel.createWebhook
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.execute
import kotlinx.coroutines.flow.firstOrNull
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

        if (discordUsername == null) {
            for (channel in config.textChannels) {
                launchJob {
                    channel.createMessage("<${minecraftUsername}> ${event.message}")
                }
            }
        } else {
            for (channel in config.textChannels) {
                launchJob {
                    @OptIn(KordExperimental::class)
                    val discordUser = channel.guild.members.firstOrNull {
                        it.username == discordUsername
                    } ?: channel.guild.getMembers(discordUsername, 1).firstOrNull()

                    if (discordUser == null || discordUser.username != discordUsername) {
                        channel.createMessage("<${minecraftUsername}> ${event.message}")
                    } else {
                        val webhook = channel.webhooks.firstOrNull { it.name == discordUser.effectiveName }
                            ?: channel.createWebhook(discordUser.effectiveName)

                        val webhookToken = webhook.token ?: return@launchJob

                        webhook.edit(webhookToken) {
                            avatar = discordUser.memberAvatar?.getImage() ?: discordUser.avatar?.getImage()
                        }

                        webhook.execute(webhookToken) {
                            content = event.message
                        }
                    }
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
