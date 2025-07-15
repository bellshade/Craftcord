# Craftcord

![Craftcord banner](<https://raw.githubusercontent.com/bellshade/Craftcord/refs/heads/main/docs/Craftcord.webp>)


Minecraft chat bridge plugin for Spigot/Paper designed for multiple Discord servers  
Currently serving Minecraft 1.21.4 and up!

---

## Development State

Code in this repository is still mostly placeholder until plugin spec is fleshed out in the Wiki
section.

However, it does work in a primitive state (it works, but with a very limited feature set) so feel
free to use it if you want to.

## There are multiple chat bridge plugins to choose from! Who is this one for?

This plugin is intended for small Minecraft servers that need multi-Discord server integration.  
A message sent in Minecraft can be sent to specific chat channels on multiple Discord servers,
and messages sent in those chat channels will be sent to Minecraft.

With the current philosophy, the Minecraft chat will be the most complete version of all the
messages, as this plugin does not handle cross-Discord server chat synchronization. It will stay
this way until there is a need for that feature.

Additionally, this plugin focuses on easy configuration. Whenever possible, configuration is doable
through the Minecraft server or in-game to increase ease of use and minimize the amount of server
restarts required. The configuration file will be synced automatically.

## Setting up and using the plugin

Please refer to the Wiki section of this repository.

## Setting up for development

Simply `./gradlew build` in your IDE of choice.  
IntelliJ is highly recommended for Kotlin-Java interop.

To get the jar file, run `./gradlew shadowJar` then get the file at `build/libs`.

If you've contributed, feel free to add your name in the `authors` section of `plugin.yml`.

## Setting up a Minecraft server for testing

### Automatic Setup

#### 1. Initialize the server

Run the `runServer` task, thanks to the Run-Task plugin (https://github.com/jpenilla/run-task)

#### 2. Accept EULA

Head over to the file `run/eula.txt` and change `eula=false` to `eula=true`,  
then re-run the `runServer` task

#### 3. Follow the plugin setup instructions in the Wiki

### Manual Setup

Refer to any PaperMC/Purpur server setup tutorial!
