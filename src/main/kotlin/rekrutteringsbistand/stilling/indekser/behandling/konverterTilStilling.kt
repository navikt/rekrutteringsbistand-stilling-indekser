package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Ad
import no.nav.pam.stilling.ext.avro.Classification
import no.nav.pam.stilling.ext.avro.RemarkType
import rekrutteringsbistand.stilling.indekser.opensearch.*
import rekrutteringsbistand.stilling.indekser.utils.log

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
        if (ad.erDirektemeldt()) ad.tittelFraStyrk() else ad.getTitle(),
        if (ad.erDirektemeldt()) ad.tittelFraKategori() else ad.getTitle()
    )
}

private fun Ad.tittelFraKategori() = tittelFraJanzz() ?: tittelFraStyrk()

private fun Ad.erDirektemeldt(): Boolean = this.getSource() == "DIR"

fun Ad.tittelFraJanzz() = getClassifications()?.maxByOrNull(Classification::getScore)?.getName()

private fun Ad.tittelFraStyrk(): String {
    val passendeStyrkkkoder = this.getCategories().filter { it.harØnsketStyrk8Format() }
    val erKladd = getPublishedByAdmin() == null

    return when (val antall = passendeStyrkkkoder.size) {
        1 -> passendeStyrkkkoder[0].getName()
        0 -> {
            if (!erKladd)
                log.warn("Fant ikke styrk8 for stilling ${getUuid()} med opprettet tidspunkt ${getCreated()}")
            "Stilling uten valgt jobbtittel"
        }

        else -> {
            log.warn(
                "Forventer én 6-sifret styrk08-kode, fant $antall stykker for stilling ${getUuid()} styrkkoder:" + this.getCategories()
                    .joinToString { "${it.getStyrkCode()}-${it.getName()}" })
            passendeStyrkkkoder.map { it.getName() }.sorted().joinToString("/")
        }
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
