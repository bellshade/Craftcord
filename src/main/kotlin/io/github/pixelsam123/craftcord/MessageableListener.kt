package io.github.pixelsam123.craftcord

import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class MessageableListener(
    private val textChannels: List<TextChannel>
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.createMessage("<${event.player.name}> ${event.message}")
            }
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.createMessage("${event.player.name} joined the server")
            }
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.createMessage("${event.player.name} left the server")
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.createMessage(event.deathMessage ?: "${event.entity.name} died")
            }
        }
    }

}