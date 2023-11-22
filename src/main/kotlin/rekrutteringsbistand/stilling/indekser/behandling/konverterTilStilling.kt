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
        "whatever"
    )
}

fun tilJson(string: String): JsonNode? {
    return try {
        val json = jacksonObjectMapper().readTree(string)
        json
    } catch (exception: JsonProcessingException) {
        null
    }
}
