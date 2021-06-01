package rekrutteringsbistand.stilling.indekser.elasticsearch

import rekrutteringsbistand.stilling.indekser.stillingsinfo.Stillingsinfo

data class RekrutteringsbistandStilling(
    val stilling: Stilling,
    val stillingsinfo: Stillingsinfo?
)

data class Stilling(
    val title: String,
    val uuid: String,
    val annonsenr: String?,
    val status: String,
    val privacy: String,
    val published: String?,
    val publishedByAdmin: String?,
    val expires: String?,
    val created: String,
    val updated: String,
    val employer: Employer?,
    val categories: List<StyrkCategory>,
    val source: String,
    val medium: String,
    val businessName: String?,
    val locations: List<Location>,
    val reference: String,
    val administration: Administration?,
    val properties: Map<String, Any>,
)

data class Location(
    val address: String?,
    val postalCode: String?,
    val city: String?,
    val county: String?,
    val countyCode: String?,
    val municipal: String?,
    val municipalCode: String?,
    val latitue: String?,
    val longitude: String?,
    val country: String,
)

data class StyrkCategory(
    val styrkCode: String,
    val name: String
)

data class Employer(
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
