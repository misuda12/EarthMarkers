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

package eu.warfaremc.earth

import cloud.commandframework.bukkit.CloudBukkitCapabilities
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.minecraft.extras.MinecraftHelp
import cloud.commandframework.paper.PaperCommandManager
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import eu.warfaremc.earth.kotson.*
import eu.warfaremc.earth.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import mu.KotlinLogging
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.command.CommandSender
import org.bukkit.configuration.Configuration
import org.bukkit.plugin.java.JavaPlugin
import org.dynmap.markers.MarkerAPI
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit

@PublishedApi
internal lateinit var plugin: EarthMarkers
internal lateinit var kguava: Cache<Any, Any>

@PublishedApi
internal lateinit var configuration: Configuration

class EarthMarkers : JavaPlugin(), CoroutineScope by MainScope() {

    val logger by lazy { KotlinLogging.logger("EarthMarkers") }
    internal val session = UUID.randomUUID().toString()

    // Command stuff
    lateinit var audiences: BukkitAudiences
    lateinit var commandManager: PaperCommandManager<CommandSender>
    // lateinit var commandConfirmationManager: CommandConfirmationManager<CommandSender>
    lateinit var commandHelp: MinecraftHelp<CommandSender>

    @PublishedApi
    internal var fisql: Database? = null
        private set
    internal var mesql: Database? = null
        private set

    @PublishedApi
    internal val context: ClassLoader by lazy { this::class.java.classLoader }

    init {
        plugin = this
        kguava = CacheBuilder.newBuilder()
            .expireAfterWrite(Long.MAX_VALUE, TimeUnit.DAYS)                                                            // Never expirable cache
            .build()
        if (dataFolder.exists() == false)
            dataFolder.mkdir().also { logger.info { "[IO] dataFolder ~'${dataFolder.path}' created" } }
        mesql = Database.connect(
            url = "jdbc:sqlite::memory:",
            user = "proxy",
            password = session,
            driver = "org.sqlite.JDBC"
        )
        configuration = config
    }

    internal val database: Database get() {
        if (fisql != null)
            return fisql!!
        if (mesql != null)
            return mesql!!
        mesql = Database.connect(
            url = "jdbc:sqlite::memory:",
            user = "proxy",
            password = session,
            driver = "org.sqlite.JDBC"
        )
        logger.info("Connected to ${mesql!!.url}, productName: ${mesql!!.vendor}, " +
                "productVersion: ${mesql!!.version}, logger: $logger, dialect: ${mesql!!.dialect}")
        return mesql!!
    }

    @PublishedApi
    internal var markerAPI: MarkerAPI? = null
        private set

    // markerAPI.getMarkerSet(id)
    //     * @param id - ID of marker set
    //     * @param label - Label for the marker
    //     * @param world - world ID
    //     * @param x - x coord
    //     * @param y - y coord
    //     * @param z - z coord
    //     * @param icon - Icon for the marker - MarkerIcon
    //     * @param is_persistent - if true, marker is persistent - Must be true

    override fun onEnable() {
        val time = System.nanoTime()
        configuration.options().copyDefaults(true)
        saveConfig()
        if (config.getStringList("api_keys").isEmpty()) {
            logger.error { "Config value 'api_keys' is empty or null" }.also { server.pluginManager.disablePlugin(this) }
            return
        }
        val fisqlResult = kotlin.runCatching {
            fisql = Database.connect(
                url = "jdbc:sqlite:$dataFolder${File.separator}earthmarkers.db",
                driver = "org.sqlite.JDBC"
            )
            logger.info("Connected to ${fisql!!.url}, productName: ${fisql!!.vendor}, " +
                    "productVersion: ${fisql!!.version}, logger: $logger, dialect: ${fisql!!.dialect}")
        }
        if (fisqlResult.isFailure)
            logger.error { "Failed to initialize database: 'fisql@jdbc:sqlite:$dataFolder${File.separator}earthmarkers.db'" }
                .also { logger.error { fisqlResult.exceptionOrNull() } }
        transaction(database) {
            SchemaUtils.create(Continent, Country, City)
        }
        if (server.pluginManager.isPluginEnabled("dynmap")) {
            val dynmap = org.dynmap.bukkit.DynmapPlugin.plugin
            if (dynmap.markerAPIInitialized()) {
                markerAPI = dynmap.markerAPI
                markerAPI!!.getMarkerSet("m_countries")
                    ?: markerAPI!!.createMarkerSet("m_countries", "Countries", setOf(markerAPI!!.getMarkerIcon("world")), false)
                markerAPI!!.getMarkerSet("m_cities")
                    ?: markerAPI!!.createMarkerSet("m_cities", "Cities", setOf(markerAPI!!.getMarkerIcon("shield")), false)
            }
            else {
                server.pluginManager.disablePlugin(this)
            }
        } else {
            server.pluginManager.disablePlugin(this)
        }
        logger.warn { "Using primary database: '${database.url}, productName: ${database.vendor}, " +
                "productVersion: ${database.version}, logger: $logger, dialect: ${database.dialect}'" }
        if (configuration.getBoolean("resolved", false) == false) ModelResolver.resolve() else ModelResolver.updateMarkers()
        // Command CommandFramework
        val executionCoordinatorFunction =
            AsynchronousCommandExecutionCoordinator.newBuilder<CommandSender>().build()
        try {
            commandManager = PaperCommandManager(
                this,
                executionCoordinatorFunction,
                ::identity,
                ::identity
            )
        } catch (exception: Exception) {
            logger.error { "Failed to initialize CommandFramework::CommandManager" }
        }
        finally {
            audiences = BukkitAudiences.create(this)
            commandHelp = MinecraftHelp("/earth help", audiences::sender, commandManager)
            if (commandManager.queryCapability(CloudBukkitCapabilities.BRIGADIER))
                commandManager.registerBrigadier()
            if (commandManager.queryCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION))
                commandManager.registerAsynchronousCompletions()
            logger.info { "Successfully installed CommandFramework Cloud 1.3" }
            CommandResolver.resolve()
        }
        logger.info { "Enabled in: §a${(System.nanoTime() - time) / 1000000}ms" }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}

class RegionDeserializer : JsonDeserializer<RegionObject> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): RegionObject {
        val jsonObject = json as JsonObject
        var a0 = 0.0
        var a1 = 0.0
        val b0 = jsonObject["latlng"].nullJsonArray
        if (b0 != null && b0.size() > 0) {
            a0 = try {
                b0[0].double
            } catch (ignored: Exception) {
               0.0
            }
            a1 = try {
                b0[1].double
            } catch (ignored: Exception) {
                0.0
            }
        }
        return RegionObject(
            jsonObject["name"].string,
            jsonObject["nativeName"].string,
            jsonObject["alpha2Code"].string,
            jsonObject["alpha3Code"].nullString,
            jsonObject["numericCode"].nullString ?: "000",
            jsonObject["capital"].nullString,
            a0,
            a1
        )
    }
}

data class RegionObject(
    @SerializedName("name") val name: String,
    @SerializedName("nativeName") val nativeName: String,
    @SerializedName("alpha2Code") val code2: String,
    @SerializedName("alpha3Code") val code3: String?,
    @SerializedName("numericCode") val countryNumber: String?,
    @SerializedName("capital") val capitalCity: String?,
    val lat: Double,
    val lng: Double
) : java.io.Serializable

fun <T> identity(t: T): T = t