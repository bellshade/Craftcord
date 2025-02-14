package io.github.pixelsam123.craftcord

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener(
    private val textChannels: List<Long>, private val kord: Kord
) : Listener {

    @EventHandler
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        for (channelId in textChannels) {
            CoroutineScope(Dispatchers.IO).launch {
                val channel = kord.getChannelOf<TextChannel>(Snowflake(channelId))

                if (channel === null) {
                    event.player.sendMessage(
                        "There is an invalid Discord text channel ID, this message will not be sent to one of the configured servers."
                    )
                } else {
                    channel.createMessage("<${event.player.name}> ${event.message}")
                }
            }
        }
    }

}