package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.*
import java.time.LocalDateTime

class FakeStillingConsumer(private val stillingMottattService: StillingMottattService) : StillingConsumer {

    override fun start() {
        val stilling = Ad(
            "uuid",
            "tittel",
            AdStatus.ACTIVE,
            PrivacyChannel.INTERNAL_NOT_SHOWN,
            Administration(
                AdministrationStatus.DONE,
                listOf(RemarkType.FOREIGN_JOB),
                "kommentar",
                "reportee",
                "navIdent"
            ),
            "published",
            "expires",
            "created",
            "updated",
            Company(
                "navn",
                "publicname",
                "orgnr",
                "parentOrgnr",
                "orgform"
            ),
            listOf(StyrkCategory("kode", "name")),
            "source",
            "medium",
            "reference",
            "publishedByAdmin",
            "businessName",
            listOf(
                Location(
                    "address",
                    "postalCode",
                    "county",
                    "municipal",
                    "country",
                    "latitue",
                    "longitude",
                    "municipal_code",
                    "county_code"
                )
            ),
            listOf(Property("key", "value"))
        )
        stillingMottattService.behandleStilling(stilling)
    }
}

