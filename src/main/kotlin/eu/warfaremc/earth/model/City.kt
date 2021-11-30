package eu.warfaremc.earth.model

import org.jetbrains.exposed.sql.Table

object City : Table("t_cities") {
    val name = varchar("name", length = 200)
    val uuid = uuid("uuid")
    val nativeName = varchar("nativeName", length = 200)
    val countryCode2 = (varchar("country_code2", length = 200) references Country.code2)
    val lat = double("lat")
    val lng = double("lng")
    val population = integer("population").default(0)
    val modifiedAt = varchar("modifiedAt", length = 10)
    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(name, name = "PK_City_ID")
}