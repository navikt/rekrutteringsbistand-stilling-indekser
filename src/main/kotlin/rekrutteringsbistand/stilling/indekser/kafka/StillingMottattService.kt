package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.Ad
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.utils.log

class StillingMottattService(private val esService: ElasticSearchService) {

    fun behandleStilling(ad: Ad) {
        val stilling = konverterTilStilling(ad)
        log.info("Behandler stilling med id: ${stilling.uuid}")
        esService.indekser(stilling)
    }

    private fun konverterTilStilling(ad: Ad): Stilling {
        return Stilling(
            ad.getTitle(),
            ad.getUuid(),
            ad.getStatus().name,
            ad.getPrivacy().name,
            ad.getPublished(),
            ad.getExpires(),
            ad.getCreated(),
            ad.getUpdated(),
            ad.getEmployer().let {
                Company(
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
            ad.getPublishedByAdmin(),
            ad.getBusinessName(),
            ad.getLocations().map { Location(
                it.getAddress(),
                it.getPostalCode(),
                it.getCounty(),
                it.getMunicipal(),
                it.getCountry(),
                it.getLatitude(),
                it.getLongitude(),
                it.getMunicipal(),
                it.countyCode
            ) },
            ad.getReference(),
            ad.getAdministration().let {
                Administration(
                    it.getStatus().name,
                    it.getRemarks().map { remark -> remark.name },
                    it.getComments(),
                    it.getReportee(),
                    it.getNavIdent()
                )
            },
            ad.getProperties().map {
                Property(it.getKey(), it.getValue())
            }
        )
    }
}
