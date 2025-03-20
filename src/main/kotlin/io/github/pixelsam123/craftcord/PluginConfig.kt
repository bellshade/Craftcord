package io.github.pixelsam123.craftcord

import dev.kord.core.entity.channel.TextChannel

data class PluginConfig(
    val textChannels: List<TextChannel>,
)
