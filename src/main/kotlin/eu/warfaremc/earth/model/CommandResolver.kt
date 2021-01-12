/*
 * This file is part of Millennium, licensed under the MIT License.
 *
 * Copyright (C) 2020 Millennium & Team
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package eu.warfaremc.earth.model

import cloud.commandframework.Description
import cloud.commandframework.annotations.Argument
import cloud.commandframework.annotations.CommandDescription
import cloud.commandframework.annotations.CommandPermission
import cloud.commandframework.annotations.Flag
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.flags.CommandFlag
import cloud.commandframework.arguments.standard.DoubleArgument
import cloud.commandframework.arguments.standard.StringArgument
import cloud.commandframework.arguments.standard.StringArrayArgument
import cloud.commandframework.bukkit.parsers.PlayerArgument
import cloud.commandframework.kotlin.MutableCommandBuilder
import cloud.commandframework.kotlin.extension.buildAndRegister
import cloud.commandframework.kotlin.extension.description
import eu.warfaremc.earth.miscellaneous.convertLatlng
import eu.warfaremc.earth.plugin
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.entity.Player
import java.io.Serializable
import java.util.function.BiFunction

class CommandResolver {
    companion object {
        fun resolve() {
            plugin.commandManager.buildAndRegister(
                "earth"
            ) {
                commandDescription("Provides commands for EarthMC")
                permission = "earth.command"
                senderType<CommandSender>()
                handler {
                    it.sender.sendMessage(
                        """
                        *----------------------------------------------*
                         Earth: Created by Mirayi_, misuda12, [warfare]
                         Credits: [
                         - (2.0.5) https://restcountries.eu/~credits
                         - (4.0.0) https://www.geonames.org/~credits
                         - (2021O) https://opencagedata.com/~credits
                         ]
                       * ----------------------------------------------*
                    """.trimIndent()
                    )
                }

                // /earth help
                registerCopy {
                    literal("help")
                    commandDescription("Shows /earth commands in help message")
                    argument {
                        StringArgument.greedy("query")
                    }
                    handler {
                        val query = it.get("query") as String?
                        plugin.commandHelp.queryCommands(
                            when (query) {
                                null -> "earth"
                                else -> "earth $query"
                            }, it.sender
                        )
                    }
                }
                
                // /earth teleport <lat> <long> [--player <player>]
                registerCopy {
                    literal("teleport")
                    permission = "earth.command.teleport"
                    senderType<Player>()
                    argument(description("latitude")) { DoubleArgument.of("lat") }
                    argument(description("lgtitude")) { DoubleArgument.of("lng") }
                    flag("player", description = description("Player optional flag")) {
                        PlayerArgument.of("player")
                    }
                    handler { context ->
                        plugin.commandManager.taskRecipe().begin(context).asynchronous {
                            val optional = it.getOptional<Player>("player")
                            if (optional.isPresent)
                                it.sender.sendMessage("[Earth][Debug] Tests passed: ${optional.get().name}")
                            val c0 = convertLatlng(it.get("lat"), it.get("lng"))
                            (it as Player).teleport(
                                Location(
                                    it.world,
                                    c0.first,
                                    255.0,
                                    c0.second
                                )
                            )
                            it.sender.sendMessage("[Earth] Teleported to: ${c0.first}, 255, ${c0.second}")
                        }.execute()
                    }
                }

                // /earth teleport-cities <ID> [--player <player>]
                registerCopy {
                    literal("teleport-cities")
                    permission = "earth.command.teleport"
                    senderType<Player>()
                    argument {
                        StringArrayArgument.of("ID", BiFunction { t, u -> return@BiFunction emptyList() })
                    }
                    handler {

                    }
                }

                // /earth teleport-states <ID> [--player <player>]
                registerCopy {
                    literal("teleport-states")
                    permission = "earth.command.teleport"
                    senderType<Player>()
                    argument {
                        StringArrayArgument.of("ID", BiFunction { t, u -> return@BiFunction emptyList() })
                    }
                    handler {

                    }
                }
            }
        }
    }
}

fun <C : Any> MutableCommandBuilder<C>.flag(
    literal: String,
    aliases: Array<String> = emptyArray(),
    description: Description = Description.empty(),
    argumentSupplier: () -> CommandArgument<C, *>
): MutableCommandBuilder<C> = mutate {
    it.flag(
        CommandFlag.newBuilder(literal)
            .withAliases(*aliases)
            .withDescription(description)
            .withArgument(argumentSupplier())
            .build()
    )
}

//@CommandMethod("earth teleport <lat> <lng>")
@CommandDescription("Teleports player to converted IRL lat, lng")
@CommandPermission("earth.command.teleport")
fun teleportPlayer(
    sender: Player,
    @Argument("lat") latitude: Double,
    @Argument("lng") lgtitude: Double,
    @Flag("player") player: Player?
) {
    val c0 = convertLatlng(latitude, lgtitude)
    when (player != null) {
        true -> {
            player.teleport(
                Location(
                    sender.world,
                    c0.first,
                    255.0,
                    c0.second,
                    player.location.yaw,
                    player.location.pitch
                )
            )
            player.sendMessage("[Earth] '${sender.name}': Teleported to: ${c0.first}, 255, ${c0.second}")
        }
        false -> {
            sender.teleport(
                Location(
                    sender.world,
                    c0.first,
                    255.0,
                    c0.second,
                    sender.location.yaw,
                    sender.location.pitch
                )
            )
            sender.sendMessage("[Earth] Teleported to: ${c0.first}, 255, ${c0.second}")
        }
    }
}

@SerializableAs("Vector2D")
data class Vector2D(
    val x: Double,
    val z: Double
): Serializable, Cloneable, ConfigurationSerializable {
    override fun serialize(): MutableMap<String, Any> = mutableMapOf(
        "x" to x,
        "y" to z
    )
}