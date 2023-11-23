package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Ad
import no.nav.pam.stilling.ext.avro.RemarkType
import rekrutteringsbistand.stilling.indekser.opensearch.*

fun konverterTilStilling(ad: Ad): Stilling {
    return Stilling(
        ad.getTitle(),
        ad.getUuid(),
        ad.getAdnr(),
        ad.getStatus().name,
        ad.getPrivacy().name,
        ad.getPublished(),
        ad.getPublishedByAdmin(),
        ad.getExpires(),
        ad.getCreated(),
        ad.getUpdated(),
        ad.getEmployer()?.let {
            Employer(
                it.getName(),
                it.getPublicName(),
                it.getOrgnr(),
                it.getParentOrgnr(),
                it.getOrgform()
            )
        },
        ad.getCategories().map { StyrkCategory(it.getStyrkCode(), it.getName()) },
        ad.getSource(),
        ad.getMedium(),
        ad.getBusinessName(),
        ad.getLocations().map {
            Location(
                it.getAddress(),
                it.getPostalCode(),
                it.getCity(),
                it.getCounty(),
                it.countyCode,
                it.getMunicipal(),
                it.municipalCode,
                it.getLatitude(),
                it.getLongitude(),
                it.getCountry()
            )
        },
        ad.getReference(),
        ad.getAdministration()?.let {
            Administration(
                it.getStatus().name,
                it.getRemarks().map(RemarkType::name),
                it.getComments(),
                it.getReportee(),
                it.getNavIdent()
            )
        },
        ad.getProperties().associate { it.getKey() to (tilJson(it.getValue()) ?: it.getValue()) },
        ad.getContacts()
            ?.map {
                Contact(
                    it.getName(),
                    it.getRole(),
                    it.getTitle(),
                    it.getEmail(),
                    it.getPhone()
                )
            } ?: emptyList(),
        if (ad.erDirektemeldt()) ad.getCategories().tittelFraStyrk() else ad.getTitle()
    )
}

private fun Ad.erDirektemeldt(): Boolean = this.getSource() == "DIR"

private fun List<no.nav.pam.stilling.ext.avro.StyrkCategory>.tittelFraStyrk(): String {
    val passendeStyrkkkoder = this.filter { it.harØnsketStyrk8Format() }

    when (val antall = passendeStyrkkkoder.size) {
        1 -> return passendeStyrkkkoder[0].getName()
        0 -> throw RuntimeException("Fant ikke styrk8 for stilling")
        else -> throw RuntimeException("Forventer en 6-sifret styrk08-kode, fant $antall stykker")
    }
}

private val styrk08SeksSiffer = Regex("""^[0-9]{4}\.[0-9]{2}$""")

private fun no.nav.pam.stilling.ext.avro.StyrkCategory.harØnsketStyrk8Format(): Boolean =
    this.getStyrkCode().matches(styrk08SeksSiffer)

fun tilJson(string: String): JsonNode? {
    return try {
        val json = jacksonObjectMapper().readTree(string)
        json
    } catch (exception: JsonProcessingException) {
        null
    }
}
