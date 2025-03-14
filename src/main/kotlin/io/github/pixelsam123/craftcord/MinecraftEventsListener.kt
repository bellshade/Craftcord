package io.github.pixelsam123.craftcord

import dev.kord.core.entity.channel.TextChannel
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class MinecraftEventsListener(
    private val plugin: Craftcord, private val textChannels: List<TextChannel>
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        for (channel in textChannels) {
            plugin.launchJob {
                channel.createMessage("<${event.player.name}> ${event.message}")
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerCount = Bukkit.getOnlinePlayers().size
        val playerWord = if (playerCount == 1) "player" else "players"

        for (channel in textChannels) {
            plugin.launchJob {
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

        for (channel in textChannels) {
            plugin.launchJob {
                channel.createMessage("${event.player.name} left the server ($playerCount $playerWord online)")
            }
        }

        plugin.setBotStatus("$playerCount $playerWord online")
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        for (channel in textChannels) {
            plugin.launchJob {
                channel.createMessage(event.deathMessage ?: "${event.entity.name} died")
            }
        }
    }

}
