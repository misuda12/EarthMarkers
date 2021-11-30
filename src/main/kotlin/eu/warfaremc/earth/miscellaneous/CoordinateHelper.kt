package eu.warfaremc.earth.miscellaneous

const val block = 3072
const val tiles = 15
                                               // X       Z
fun convertLatlng(lat: Double, lng: Double): Pair<Double, Double> {
    return Pair(
        Math.round(lng * block / tiles).toDouble(),
        (-1 * Math.round(lat * block / tiles).toDouble())
    )
}