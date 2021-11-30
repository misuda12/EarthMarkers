package eu.warfaremc.earth.model

import eu.warfaremc.earth.kguava
import eu.warfaremc.earth.miscellaneous.convertLatlng
import net.pl3x.map.api.Key
import net.pl3x.map.api.MapWorld
import net.pl3x.map.api.Point
import net.pl3x.map.api.SimpleLayerProvider
import net.pl3x.map.api.marker.Icon
import net.pl3x.map.api.marker.Marker
import net.pl3x.map.api.marker.MarkerOptions
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

data class Marker(
    val id: UUID,
    val name: String,
    val description: String,
    val world: String,
    val locX: Double,
    val locY: Double = 255.0,
    val locZ: Double,
    val yaw: Float = 1F,
    val pitch: Float = 1F
) {
    class MapTask(
        private val key: String,
        private val world: MapWorld,
        private val provider: SimpleLayerProvider
    ) : BukkitRunnable() {

        private var stop = false

        /**
         * When an object implementing interface `Runnable` is used
         * to create a thread, starting the thread causes the object's
         * `run` method to be called in that separately executing
         * thread.
         *
         *
         * The general contract of the method `run` is that it may
         * take any action whatsoever.
         *
         * @see java.lang.Thread.run
         */
        override fun run() {
            if (stop)
                cancel()
            provider.clearMarkers()
            kguava.getIfPresent("markers_$key")?.forEach {
                handle(it.id, it.name, it.description, it.world, it.locX, it.locZ)
            }
        }

        private fun handle(
            id: UUID,
            name: String,
            description: String,
            world: String,
            locX: Double,
            locZ: Double
        ) {
            val icon: Icon = Marker.icon(
                Point.point(locX, locZ), Key.of("pl3xmarker_marker_icon_$key"), 16
            )
            icon.markerOptions(MarkerOptions.builder()
                .hoverTooltip("<center>$name<br/>$description</center>"))
            val markerId = "pl3xmarker_${world}_marker_$id"
            provider.addMarker(Key.key(markerId), icon)
        }

        fun disable() {
            cancel()
            stop = true
            provider.clearMarkers()
        }
    }
}