package io.github.pixelsam123.craftcord

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MinecraftCommandsHandler(
    private val plugin: Craftcord
) : TabExecutor {
    override fun onTabComplete(
        sender: CommandSender, command: Command, label: String, args: Array<String>
    ): List<String> {
        return listOf("help", "read_config")
    }

    override fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<String>
    ): Boolean {
        when (args.firstOrNull()) {
            "help" -> {
                sender.sendMessage(
                    """
                    Craftcord commands:
                      /craftcord help - Displays this help message.
                      /craftcord read_config - Reads the live configuration being used by the plugin, except the Discord bot token of course.
                """.trimIndent()
                )
                return true
            }

            "read_config" -> {
                sender.sendMessage(
                    """
                    Text Channels:
${plugin.pluginConfig.textChannels.joinToString("\n") { "                      - ${it.name}" }}
                    Minecraft username to Discord username:
${
                        plugin.pluginConfig
                            .minecraftUsernameToDiscordUsername
                            .map { (minecraftUsername, discordUsername) -> "                      - $minecraftUsername -> $discordUsername" }
                            .joinToString("\n")
                    }
                """.trimIndent())
                return true
            }

            "reload" -> {
                sender.sendMessage("Reloading configuration...")
                plugin.loadConfig()
                sender.sendMessage("Configuration reloaded successfully.")
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