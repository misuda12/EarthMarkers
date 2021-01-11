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

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import eu.warfaremc.earth.*
import eu.warfaremc.earth.configuration
import eu.warfaremc.earth.kguava
import eu.warfaremc.earth.miscellaneous.DownloadHelper
import eu.warfaremc.earth.miscellaneous.convertLatlng
import eu.warfaremc.earth.plugin
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.Serializable
import java.io.StringReader
import java.net.URL
import java.util.*
import kotlin.math.round

class ModelResolver {
    companion object {
        val geoDB_DIR = File("${plugin.dataFolder}${File.separator}geoDB")
        fun resolve() {
            plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
                plugin.logger.info { "Resolving geoDB ..." }
                if (geoDB_DIR.exists() == false)
                    geoDB_DIR.mkdirs()
                ContinentsEnum.values().forEachIndexed { index, continent ->
                    transaction(plugin.database) {
                        val c0 = Continent.selectAll()
                        val condition =
                            c0.filterNotNull().any { it[Continent.name] == continent.formerName }
                        if (condition)
                            return@transaction
                        plugin.logger.info { "[Continent[" + String.format("%04d", index) + "]] Resolving: [" + continent.code2 + "]" + continent.formerName }
                        Continent.insertIgnore {
                            it[name] = continent.formerName
                            it[uuid] = UUID.randomUUID()
                            it[code2] = continent.code2
                        }
                    }
                    runBlocking {
                        when (continent) {
                            ContinentsEnum.ANTARCTICA    -> {
                                val countries = listOf(
                                    RegionObject(
                                        "Antarctica",
                                        "Antarctica",
                                        "AQ",
                                        "ATA",
                                        "010",
                                        null,
                                        -69.6354154,
                                        0.0
                                    ),
                                    RegionObject(
                                        "Bouvet Island",
                                        "Bouvet Island",
                                        "BV",
                                        "BVT",
                                        "074",
                                        null,
                                        -54.4221749,
                                        3.3605883
                                    ),
                                    RegionObject(
                                        "South Georgia And The South Sandwich Islands",
                                        "South Georgia And The South Sandwich Islands",
                                        "GS",
                                        "SGS",
                                        "239",
                                        null,
                                        -54.4306908,
                                        -36.9869112
                                    ),
                                    RegionObject(
                                        "Heard and Mc Donald Islands",
                                        "Heard and Mc Donald Islands",
                                        "HM",
                                        "HMD",
                                        "334",
                                        null,
                                        -53.0765818,
                                        73.5136616
                                    ),
                                    RegionObject(
                                        "French Southern Territories",
                                        "French Southern Territories",
                                        "TF",
                                        "ARF",
                                        "260",
                                        null,
                                        -49.1306765,
                                        69.5828104
                                    )
                                )
                                countries.forEachIndexed { index, region ->
                                    transaction(plugin.database) {
                                        val c1 = Country  .selectAll()
                                        val condition =
                                            c1.filterNotNull().any { it[Country.name] == region.name }
                                        if (condition)
                                            return@transaction
                                        plugin.logger.info { "[Country[" + String.format("%04d", index) + "]] [" + region.code2 + "] LocRadX[" + round(region.lat) + ", " + round(region.lng) + "]\t"+ region.name}
                                        Country.insertIgnore {
                                            it[name] = region.name
                                            it[uuid] = UUID.randomUUID()
                                            it[nativeName] = region.nativeName
                                            it[code2] = region.code2
                                            it[code3] = region.code3
                                            it[capitalCity] = region.capitalCity
                                            it[countryNumber] = region.countryNumber
                                            it[continentCode2] = continent.code2
                                            it[lat] = region.lat
                                            it[lng] = region.lng
                                        }
                                    }
                                }
                                kguava.put("continent.${continent.formerName.toLowerCase()}.countries", countries)
                            }
                            else -> {
                                val former: Result<String> = kotlin.runCatching {
                                    URL("https://restcountries.eu/rest/v2/region/${continent.alias!!.toLowerCase()}")
                                        .readText()
                                }
                                if (former.isSuccess && former.getOrNull() != null) {
                                    kguava.put("continent.${continent.formerName.toLowerCase()}", former.getOrNull()!!)
                                    val result: Result<List<RegionObject>> = kotlin.runCatching {
                                        val reader = StringReader(former.getOrNull()!!)
                                        val gson = GsonBuilder().serializeNulls()
                                            .registerTypeAdapter(RegionObject::class.java, RegionDeserializer())
                                            .create()
                                        gson.fromJson(reader, Array<RegionObject>::class.java).toList()
                                    }
                                    if (result.isSuccess && result.getOrNull().isNullOrEmpty() == false) {
                                        kguava.put(
                                            "continent.${continent.formerName.toLowerCase()}.countries",
                                            result.getOrNull()!!
                                        )
                                        result.getOrNull()!!.forEachIndexed { index, region ->
                                            transaction(plugin.database) {
                                                val c1 = Country  .selectAll()
                                                val condition0 = continent != ContinentsEnum.SOUTH_AMERICA && south_america.split("\n")
                                                    .map { it.split("\t")[0] }
                                                    .any { region.code2 == it }
                                                val condition1 =
                                                    c1.filterNotNull().any { it[Country.name] == region.name }
                                                if (condition0)
                                                    return@transaction
                                                if (condition1)
                                                    return@transaction
                                                plugin.logger.info { "[Country[" + String.format("%04d", index) + "]] [" + region.code2 + "] LocRadX[" + round(region.lat) + ", " + round(region.lng) + "]\t"+ region.name}
                                                Country.insertIgnore {
                                                    it[name] = region.name
                                                    it[uuid] = UUID.randomUUID()
                                                    it[nativeName] = region.nativeName
                                                    it[code2] = region.code2
                                                    it[code3] = region.code3
                                                    it[capitalCity] = region.capitalCity
                                                    it[countryNumber] = region.countryNumber
                                                    it[continentCode2] = continent.code2
                                                    it[lat] = region.lat
                                                    it[lng] = region.lng
                                                }
                                            }
                                        }
                                    }
                                    if (result.isFailure)
                                        plugin.logger.error { "Failed to resolve countries of: $continent" }
                                }
                            }
                        }
                    }
                }
                val scope = configuration.get("scope", 15000) as Int
                val former: Result<File> = kotlin.runCatching {
                    plugin.logger.info { "Downloading geoCity metadata ..." }
                    DownloadHelper.handleArchive(
                        URL("http://download.geonames.org/export/dump/cities$scope.zip"),
                        geoDB_DIR
                    )
                }
                if (former.isSuccess) {
                    val cities = File(geoDB_DIR, "cities$scope.txt")
                    if (cities.exists() == false) {
                        plugin.logger.error { "Failed to read cities data: NOT_EXISTS" }
                        return@Runnable
                    }

                    /**
                    0  geonameid         : integer id of record in geonames database
                    1  name              : name of geographical point (utf8) varchar(200)
                    2  asciiname         : name of geographical point in plain ascii characters, varchar(200)
                    3  alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
                    4  latitude          : latitude in decimal degrees (wgs84)
                    5  longitude         : longitude in decimal degrees (wgs84)
                    6  feature class     : see http://www.geonames.org/export/codes.html, char(1)
                    7  feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
                    8  country code      : ISO-3166 2-letter country code, 2 characters
                    9  cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
                    10  admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
                    11 admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
                    12 admin3 code       : code for third level administrative division, varchar(20)
                    13 admin4 code       : code for fourth level administrative division, varchar(20)
                    14 population        : bigint (8 byte int)
                    15 elevation         : in meters, integer
                    16 dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
                    17 timezone          : the iana timezone id (see file timeZone.txt) varchar(40)
                    18 modification date : date of last modification in yyyy-MM-dd format
                     */

                    /**
                    0  geonameid         : integer id of record in geonames database
                    1  name              : name of geographical point (utf8) varchar(200)
                    2  asciiname         : name of geographical point in plain ascii characters, varchar(200)
                    3  alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
                    4  latitude          : latitude in decimal degrees (wgs84)
                    5  longitude         : longitude in decimal degrees (wgs84)
                    6  feature class     : see http://www.geonames.org/export/codes.html, char(1)
                    7  feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
                    8  country code      : ISO-3166 2-letter country code, 2 characters
                    9  cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
                    10  admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
                    11 admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
                    12 admin3 code       : code for third level administrative division, varchar(20)
                    13 admin4 code       : code for fourth level administrative division, varchar(20)
                    14 population        : bigint (8 byte int)
                    15 elevation         : in meters, integer
                    16 dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
                    17 timezone          : the iana timezone id (see file timeZone.txt) varchar(40)
                    18 modification date : date of last modification in yyyy-MM-dd format
                     */

                    /*
                    cities.useLines { sequence ->
                        sequence.filterNotNull()
                            .forEachIndexed { index, line ->
                                val segments = line.split("\t")
                                if (segments.isEmpty()) {
                                    plugin.logger.error { "Failed to read line: L_$index" }
                                    return@forEachIndexed
                                }
                                val named = segments[1]
                                val entry = GeoNameEntry(
                                    named,
                                    segments[2],
                                    covertDegrees(segments[4]),
                                    covertDegrees(segments[5]),
                                    segments[8],
                                    segments[14].toInt(),
                                    segments[18]
                                )
                                kguava.put("cities.$named", entry)
                                transaction(plugin.database) {
                                    val c2 = City     .selectAll()
                                    val condition =
                                        c2.filterNotNull().any { it[City.name] == entry.name }
                                    if (condition)
                                        return@transaction
                                    plugin.logger.info {
                                        "[City] Resolving($index of " + (sequence.filterNotNull()
                                            .count() - 1) + "): [" + entry.countryCode2 + "]" + named + "\t" + "LocRadX[" + entry.lat + ", " + entry.lng + "]"
                                    }
                                    City.insertIgnore {
                                        it[name] = entry.name
                                        it[uuid] = UUID.randomUUID()
                                        it[nativeName] = entry.nativeName
                                        it[countryCode2] = entry.countryCode2
                                        it[lat] = entry.lat
                                        it[lng] = entry.lng
                                        it[population] = entry.population
                                        it[modifiedAt] = entry.modifiedAt
                                    }
                                }
                            }
                    }
                     */
                }
                plugin.logger.info { "FINISHED" }
                if (former.isFailure)
                    plugin.logger.error { "Failed to resolve cities data: " + former.exceptionOrNull()!! }
                updateMarkers()
            })
        }

        fun updateMarkers() {
            val s0 = plugin.markerAPI!!.getMarkerSet("m_countries")
                ?: plugin.markerAPI!!.createMarkerSet("m_countries", "Countries", setOf(plugin.markerAPI!!.getMarkerIcon("world")), false)
            val s1 = plugin.markerAPI!!.getMarkerSet("m_cities")
                ?: plugin.markerAPI!!.createMarkerSet("m_cities", "Cities", setOf(plugin.markerAPI!!.getMarkerIcon("shield")), false)
            transaction(plugin.database) {
                for (country in Country.selectAll()) {
                    if (country[Country.lat] != 0.0 || country[Country.lng] != 0.0) {
                        val m0 = convertLatlng(country[Country.lat], country[Country.lng])
                        val m1 = s0.findMarker(country[Country.uuid].toString())
                            ?: s0.createMarker(country[Country.uuid].toString(), country[Country.name], "world", m0.first, 255.0, m0.second, plugin.markerAPI!!.getMarkerIcon("world"), false)
                        if (country[Country.capitalCity].isNullOrEmpty() == false) {
                            val result = City.select { City.name eq country[Country.name] }.singleOrNull()
                            if (result != null) {
                                val c0 = convertLatlng(result[City.lat], result[City.lng])
                                val c1 = s1.findMarker(result[City.uuid].toString())
                                    ?: s1.createMarker(result[City.uuid].toString(), result[City.name], "world", m0.first, 255.0, m0.second, plugin.markerAPI!!.getMarkerIcon("shield"), false)
                            }
                        }
                    }
                }
                for (city in City.selectAll()) {
                    if (city[City.lat] != 0.0 || city[City.lng] != 0.0) {
                        val c0 = convertLatlng(city[City.lat], city[City.lng])
                        val c1 = s1.findMarker(city[City.uuid].toString())
                            ?: s1.createMarker(city[City.uuid].toString(), city[City.name], "world", c0.first, 255.0, c0.second, plugin.markerAPI!!.getMarkerIcon("shield"), false)
                    }
                }
            }
        }
    }
}

data class GeoNameEntry(
    val name: String,
    @SerializedName("asciiname")    val nativeName: String,
    @SerializedName("latitude")     val lat: Double,
    @SerializedName("longitude")    val lng: Double,
    @SerializedName("countryCode2") val countryCode2: String,
    @SerializedName("population")   val population: Int,
    @SerializedName("modifiedAt")   val modifiedAt: String,
) : Serializable

private fun covertDegrees(degree: String): Double
        = degree.toDouble()