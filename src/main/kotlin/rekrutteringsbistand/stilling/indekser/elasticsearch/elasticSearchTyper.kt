package rekrutteringsbistand.stilling.indekser.elasticsearch

data class Stilling(
    val title: String,
    val uuid: String,
    val status: String,
    val privacy: String,
    val published: String?,
    val expires: String?,
    val created: String,
    val updated: String,
    val employer: Company?,
    val categories: List<StyrkCategory>,
    val source: String,
    val medium: String,
    val publishedByAdmin: String?,
    val businessName: String?,
    val locations: List<Location>,
    val reference: String,
    val administration: Administration?,
    val properties: List<Property>,
)

data class Property(
    val key: String,
    val value: String
)

data class Location(
    val address: String?,
    val postalCode: String?,
    val county: String?,
    val municipal: String?,
    val country: String,
    val latitue: String?,
    val longitude: String?,
    val municipal_code: String?,
    val county_code: String?
)

data class StyrkCategory(
    val styrkCode: String,
    val name: String
)


data class Company(
    val name: String,
    val publicName: String,
    val orgnr: String?,
    val parentOrgnr: String?,
    val orgform: String
)

data class Administration(
    val status: String,
    val remarks: List<String>,
    val comments: String,
    val reportee: String,
    val navIdent: String
)
