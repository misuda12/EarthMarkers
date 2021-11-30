package eu.warfaremc.earth.model

import org.jetbrains.exposed.sql.Table

object Continent : Table("t_continents") {
    val name = varchar("name", length = 200)
    val uuid = uuid("uuid")
    val code2 = varchar("code2", length = 2)
    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(name, name = "PK_Continent_ID")
}