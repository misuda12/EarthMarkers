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

import org.jetbrains.exposed.sql.Table

object Continent : Table("t_continents") {
    val name = varchar("name", length = 200)
    val uuid = uuid("uuid")
    val code2 = varchar("code2", length = 2)
    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(name, name = "PK_Continent_ID")
}

enum class ContinentsEnum(val formerName: String, val alias: String?, val code2: String) {
    ANTARCTICA("Antarctica", null,"AN"),
    AFRICA("Africa", "Africa", "AF"),
    ASIA("Asia", "Asia","AS"),
    EUROPE("Europe", "Europe","EU"),
    NORTH_AMERICA("North America", "Americas","NA"),
    SOUTH_AMERICA("South America", "Americas","SA"),
    OCEANIA("Oceania", "Oceania","OC")
}