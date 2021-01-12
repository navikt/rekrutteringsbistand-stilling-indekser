package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.Stillingsinfo
import java.time.LocalDateTime

val enAd = Ad(
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
    LocalDateTime.now().toString(),
    LocalDateTime.now().toString(),
    LocalDateTime.now().toString(),
    LocalDateTime.now().toString(),
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
    LocalDateTime.now().toString(),
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

val enStillingsinfo = Stillingsinfo(
    "eierNavIdent",
    "eierNavn",
    "notat",
    "stillingsid",
    "stillingsinfoid"
)
