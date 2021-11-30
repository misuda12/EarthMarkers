package eu.warfaremc.earth.miscellaneous

const val scale = 500
                                               // X       Z
fun convertLatlng(lat: Double, lng: Double): Pair<Double, Double> {
    return Pair(
        Math.round(((lng * 100.0) / 100.0) * 120000 / scale).toDouble(),
        (-1 * Math.round(((lat * 100.0) / 100.0) * 120000 / scale)).toDouble()
    )
}