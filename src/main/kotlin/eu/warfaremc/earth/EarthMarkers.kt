package eu.warfaremc.earth

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import eu.warfaremc.earth.miscellaneous.convertLatlng
import eu.warfaremc.earth.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import net.pl3x.map.api.Key
import net.pl3x.map.api.Pl3xMapProvider
import net.pl3x.map.api.SimpleLayerProvider
import org.bukkit.configuration.Configuration
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@PublishedApi
internal lateinit var plugin: EarthMarkers
internal lateinit var kguava: Cache<String, ArrayList<Marker>>

@PublishedApi
internal lateinit var configuration: Configuration

class EarthMarkers : JavaPlugin(), CoroutineScope by MainScope() {

    internal val session = UUID.randomUUID().toString()

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
    internal var provider: MutableMap<String, Marker.MapTask> = HashMap()
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
        val fisqlResult = kotlin.runCatching {
            fisql = Database.connect(
                url = "jdbc:sqlite:$dataFolder${File.separator}earthmarkers.db",
                driver = "org.sqlite.JDBC"
            )
            logger.info("Connected to ${fisql!!.url}, productName: ${fisql!!.vendor}, " +
                    "productVersion: ${fisql!!.version}, logger: $logger, dialect: ${fisql!!.dialect}")
        }
        if (fisqlResult.isFailure)
            logger.warning { "Failed to initialize database: 'fisql@jdbc:sqlite:$dataFolder${File.separator}earthmarkers.db'" }
                .also { logger.warning { fisqlResult.exceptionOrNull().toString() } }
        transaction(database) {
            SchemaUtils.create(Continent, Country, City)
        }
        if (server.pluginManager.isPluginEnabled("Pl3xMap")) {
            val url0 = ImageIO.read(URL("https://cdn.upload.systems/uploads/1zRKxN3t.png"))
            Pl3xMapProvider.get().iconRegistry()
                .register(Key.of("pl3xmarker_marker_icon_country"), url0)
            val url1 = ImageIO.read(URL("https://cdn.upload.systems/uploads/1zRKxN3t.png"))
            Pl3xMapProvider.get().iconRegistry()
                .register(Key.of("pl3xmarker_marker_icon_city"), url1)
            val mapWorld = Pl3xMapProvider.get().mapWorlds().firstOrNull { it.name() == configuration.getString("world", "world") }
            if (mapWorld == null) {
                logger.warning { "Failed to mapWorld provider for: ${configuration.getString("world", "world")}" }
                server.pluginManager.disablePlugin(this)
                return
            }
            kguava.put("markers_country", ArrayList())
            kguava.put("markers_city", ArrayList())
            transaction(database) {
                for (country in Country.selectAll()) {
                    if (country[Country.lat] != 0.0 || country[Country.lng] != 0.0) {
                        val c0 = convertLatlng(country[Country.lat], country[Country.lng])
                        kguava.getIfPresent("markers_country")?.add(
                            Marker(
                                country[Country.uuid],
                                country[Country.name],
                                "Code: " + country[Country.code2],
                                mapWorld.name(),
                                locX = c0.first,
                                locZ = c0.second
                            )
                        )
                        if (country[Country.capitalCity].isNullOrEmpty() == false) {
                            val capital = country[Country.capitalCity]
                            val city = City.select { City.name eq "$capital" }.singleOrNull()
                            if (city != null) {
                                if (city[City.lat] != 0.0 || city[City.lng] != 0.0) {
                                    val c1 = convertLatlng(city[City.lat], city[City.lng])
                                    kguava.getIfPresent("markers_city")?.add(
                                        Marker(
                                            city[City.uuid],
                                            city[City.name],
                                            "Population: " + city[City.population],
                                            mapWorld.name(),
                                            locX = c1.first,
                                            locZ = c1.second
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                for (city in City.selectAll()) {
                    if (city[City.lat] != 0.0 || city[City.lng] != 0.0) {
                        if (city[City.population] > configuration.getInt("population", 1_000_000)) {
                            val c0 = convertLatlng(city[City.lat], city[City.lng])
                            kguava.getIfPresent("markers_city")?.add(
                                Marker(
                                    city[City.uuid],
                                    city[City.name],
                                    city[City.population].toString(),
                                    mapWorld.name(),
                                    locX = c0.first,
                                    locZ = c0.second
                                )
                            )
                        }
                    }
                }
            }
            val layerProviderCountry: SimpleLayerProvider = SimpleLayerProvider.builder("Countries")
                .showControls(true)
                .defaultHidden(false)
                .build()
            val layerProviderCity: SimpleLayerProvider = SimpleLayerProvider.builder("Cities")
                .showControls(true)
                .defaultHidden(false)
                .build()
            mapWorld.layerRegistry()
                .register(Key.of("pl3marker_${mapWorld.uuid()}_marker_0"), layerProviderCountry)
            val task0 = Marker.MapTask("country", mapWorld, layerProviderCountry)
            mapWorld.layerRegistry()
                .register(Key.of("pl3marker_${mapWorld.uuid()}_marker_1"), layerProviderCity)
            val task1 = Marker.MapTask("city", mapWorld, layerProviderCity)
            task0.runTaskTimerAsynchronously(this, 0, 20L * 5)
            provider[mapWorld.uuid().toString()] = task0
            task1.runTaskTimerAsynchronously(this, 0, 20L * 5)
            provider[mapWorld.uuid().toString()] = task1
        } else {
            server.pluginManager.disablePlugin(this)
            return
        }
        logger.warning { "Using primary database: '${database.url}, productName: ${database.vendor}, " +
                "productVersion: ${database.version}, logger: $logger, dialect: ${database.dialect}'" }
        logger.info { "Enabled in: §a${(System.nanoTime() - time) / 1000000}ms" }
    }

    override fun onDisable() {
        server.scheduler.cancelTasks(this)
    }
}