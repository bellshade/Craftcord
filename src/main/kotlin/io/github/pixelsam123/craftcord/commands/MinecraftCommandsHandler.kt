package io.github.pixelsam123.craftcord.commands

import dev.kord.core.entity.channel.TextChannel
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MinecraftCommandsHandler(
    private val textChannels: List<TextChannel>
) : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        return listOf("help", "read_config")
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        when (args.firstOrNull()) {
            "help" -> {
                sender.sendMessage("""
                    Craftcord commands:
                      /craftcord help - Displays this help message.
                      /craftcord read_config - Reads the live configuration being used by the plugin, except the Discord bot token of course.
                """.trimIndent())
                return true
            }
            "read_config" -> {
                sender.sendMessage("""
                    Text Channels:
                      ${textChannels.joinToString(", ") { it.name }}
                """.trimIndent())
                return true
            }
            null -> {
                sender.sendMessage("Please specify a subcommand.")
                return false
            }
            else -> {
                sender.sendMessage("Unknown subcommand.")
                return false
            }
        }
    }

}