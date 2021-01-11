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

val africa = """
    AO	Angola
    BF	Burkina Faso
    BI	Burundi
    BJ	Benin
    BW	Botswana
    CD	Democratic Republic of Congo
    CF	Central African Republic
    CG	Congo
    CI	Cote D'Ivoire
    CM	Cameroon
    CV	Cape Verde
    DJ	Djibouti
    DZ	Algeria
    EG	Egypt
    EH	Western Sahara
    ER	Eritrea
    ET	Ethiopia
    GA	Gabon
    GH	Ghana
    GM	Gambia
    GN	Guinea
    GQ	Equatorial Guinea
    GW	Guinea-bissau
    KE	Kenya
    KM	Comoros
    LR	Liberia
    LS	Lesotho
    LY	Libyan Arab Jamahiriya
    MA	Morocco
    MG	Madagascar
    ML	Mali
    MR	Mauritania
    MU	Mauritius
    MW	Malawi
    MZ	Mozambique
    NA	Namibia
    NE	Niger
    NG	Nigeria
    RE	Reunion
    RW	Rwanda
    SC	Seychelles
    SD	Sudan
    SH	St. Helena
    SL	Sierra Leone
    SN	Senegal
    SO	Somalia
    SS	South Sudan
    ST	Sao Tome and Principe
    SZ	Swaziland
    TD	Chad
    TG	Togo
    TN	Tunisia
    TZ	Tanzania, United Republic of
    UG	Uganda
    YT	Mayotte
    ZA	South Africa
    ZM	Zambia
    ZW	Zimbabwe
""".trimIndent()

val antarctica = """
    AQ	Antarctica ATA 010 -69.6354154 0.0
    BV	Bouvet Island BVT 074 -54.4221749 3.3605883
    GS	South Georgia And The South Sandwich Islands SGS 239 -54.4306908 -36.9869112
    HM	Heard and Mc Donald Islands HMD 334 -53.0765818 73.5136616
    TF	French Southern Territories ARF 260 -49.1306765 69.5828104
""".trimIndent()

val asia = """
    AE	United Arab Emirates
    AF	Afghanistan
    AM	Armenia
    AZ	Azerbaijan
    BD	Bangladesh
    BH	Bahrain
    BN	Brunei Darussalam
    BT	Bhutan
    CC	Cocos (Keeling) Islands
    CN	China
    CX	Christmas Island
    CY	Cyprus
    GE	Georgia
    HK	Hong Kong
    ID	Indonesia
    IL	Israel
    IN	India
    IO	British Indian Ocean Territory
    IQ	Iraq
    IR	Iran (Islamic Republic of)
    JO	Jordan
    JP	Japan
    KG	Kyrgyzstan
    KH	Cambodia
    KP	North Korea
    KR	Korea, Republic of
    KW	Kuwait
    KZ	Kazakhstan
    LA	Lao People's Democratic Republic
    LB	Lebanon
    LK	Sri Lanka
    MM	Myanmar
    MN	Mongolia
    MO	Macau
    MV	Maldives
    MY	Malaysia
    NP	Nepal
    OM	Oman
    PH	Philippines
    PK	Pakistan
    PS	Palestine
    QA	Qatar
    RU	Russian Federation
    SA	Saudi Arabia
    SG	Singapore
    SY	Syrian Arab Republic
    TH	Thailand
    TJ	Tajikistan
    TL	East Timor
    TM	Turkmenistan
    TR	Turkey
    TW	Taiwan
    UZ	Uzbekistan
    VN	Viet Nam
    YE	Yemen
""".trimIndent()

val europe = """
    AD	Andorra
    AL	Albania
    AM	Armenia
    AT	Austria
    AZ	Azerbaijan
    BA	Bosnia and Herzegovina
    BE	Belgium
    BG	Bulgaria
    BY	Belarus
    CH	Switzerland
    CY	Cyprus
    CZ	Czech Republic
    DE	Germany
    DK	Denmark
    EE	Estonia
    ES	Spain
    FI	Finland
    FO	Faroe Islands
    FR	France
    GB	United Kingdom
    GE	Georgia
    GG	Guernsey
    GI	Gibraltar
    GR	Greece
    HR	Croatia
    HU	Hungary
    IE	Ireland
    IM	Isle of Man
    IS	Iceland
    IT	Italy
    KZ	Kazakhstan
    LI	Liechtenstein
    LT	Lithuania
    LU	Luxembourg
    LV	Latvia
    MC	Monaco
    MD	Moldova, Republic of
    ME	Montenegro
    MK	Macedonia
    MT	Malta
    NL	Netherlands
    NO	Norway
    PL	Poland
    PT	Portugal
    RO	Romania
    RS	Serbia
    RU	Russian Federation
    SE	Sweden
    SI	Slovenia
    SJ	Svalbard and Jan Mayen Islands
    SK	Slovak Republic
    SM	San Marino
    TR	Turkey
    UA	Ukraine
    VA	Vatican City State (Holy See)
    XK	Kosovo
""".trimIndent()

val north_america = """
    AG	Antigua and Barbuda
    AI	Anguilla
    AN	Netherlands Antilles
    AW	Aruba
    BB	Barbados
    BL	Saint Barthelemy
    BM	Bermuda
    BS	Bahamas
    BZ	Belize
    CA	Canada
    CR	Costa Rica
    CU	Cuba
    CW	Curacao
    DM	Dominica
    DO	Dominican Republic
    GD	Grenada
    GL	Greenland
    GP	Guadeloupe
    GT	Guatemala
    HN	Honduras
    HT	Haiti
    JM	Jamaica
    KN	Saint Kitts and Nevis
    KY	Cayman Islands
    LC	Saint Lucia
    MF	Saint Martin
    MQ	Martinique
    MS	Montserrat
    MX	Mexico
    NI	Nicaragua
    PA	Panama
    PM	St. Pierre and Miquelon
    PR	Puerto Rico
    SV	El Salvador
    SX	Sint Maarten
    TC	Turks and Caicos Islands
    TT	Trinidad and Tobago
    UM	United States Minor Outlying Islands
    US	United States
    VC	Saint Vincent and the Grenadines
    VG	Virgin Islands (British)
    VI	Virgin Islands (U.S.)
""".trimIndent()

val oceania = """
    AS	American Samoa
    AU	Australia
    CK	Cook Islands
    FJ	Fiji
    FM	Micronesia, Federated States of
    GU	Guam
    KI	Kiribati
    MH	Marshall Islands
    MP	Northern Mariana Islands
    NC	New Caledonia
    NF	Norfolk Island
    NR	Nauru
    NU	Niue
    NZ	New Zealand
    PF	French Polynesia
    PG	Papua New Guinea
    PN	Pitcairn
    PW	Palau
    SB	Solomon Islands
    TK	Tokelau
    TO	Tonga
    TV	Tuvalu
    UM	United States Minor Outlying Islands
    VU	Vanuatu
    WF	Wallis and Futuna Islands
    WS	Samoa
""".trimIndent()

val south_america = """
    AR	Argentina
    BO	Bolivia
    BR	Brazil
    CL	Chile
    CO	Colombia
    EC	Ecuador
    FK	Falkland Islands (Malvinas)
    GF	French Guiana
    GY	Guyana
    PE	Peru
    PY	Paraguay
    SR	Suriname
    UY	Uruguay
    VE	Venezuela
""".trimIndent()

object Country : Table("t_countries") {
    val name = varchar("name", length = 200)
    val uuid = uuid("uuid")
    val nativeName = varchar("nativeName", length = 200)
    val code2 = varchar("code2", length = 2)
    val code3 = varchar("code3", length = 3).nullable()
    val capitalCity = varchar("capital_city", length = 200).nullable()
    val countryNumber = varchar("country_number", length = 3).nullable()
    val continentCode2 = (varchar("continent_code2", length = 2) references Continent.code2)
    val lat = double("lat")
    val lng = double("lng")
    override val primaryKey: PrimaryKey?
        get() = PrimaryKey(name, name = "PK_Country_ID")
}