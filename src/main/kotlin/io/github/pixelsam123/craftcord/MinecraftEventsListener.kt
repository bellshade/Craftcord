package io.github.pixelsam123.craftcord

import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class MinecraftEventsListener(
    private val kord: Kord, private val textChannels: List<TextChannel>
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
        val playerCount = Bukkit.getOnlinePlayers().size

        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.createMessage("${event.player.name} joined the server ($playerCount players online)")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            kord.editPresence {
                state = "$playerCount players online"
            }
        }
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val playerCount = Bukkit.getOnlinePlayers().size

        for (channel in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                channel.createMessage("${event.player.name} left the server ($playerCount players online)")
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            kord.editPresence {
                state = "$playerCount players online"
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
