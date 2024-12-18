package rekrutteringsbistand.stilling.indekser.setup

import no.nav.pam.stilling.ext.avro.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.Stillingsinfo
import java.time.LocalDateTime


fun enAdMed(
    source: String = "source",
    categories: List<StyrkCategory>? = listOf(StyrkCategory("1111", "name111"), StyrkCategory("2222", "name2222"), StyrkCategory("666666", "name666666")),
    title: String = "tittel",
    classification: List<Classification>? = null
) = Ad(
    "uuid",
    "annonsenr",
    title,
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
    categories,
    source,
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
            "city",
            "country",
            "latitue",
            "longitude",
            "municipal_code",
            "county_code"
        )
    ),
    listOf(
        Property("sector", "Offentlig"),
        Property("adtext", "<h1>Tittel</h2><p>Den beste stillingen <b>noen sinne</b></p>"),
        Property("searchtags", "[{\"label\":\"Sales Promotion Manager\",\"score\":1.0},{\"label\":\"Salgssjef\",\"score\":0.25137392},{\"label\":\"Sales Manager (Hotels)\",\"score\":0.21487874},{\"label\":\"Promotions Director\",\"score\":0.09032349},{\"label\":\"Salgsfremmer\",\"score\":0.09004237}]"),
        Property("tags", "[\"INKLUDERING__ARBEIDSTID\", \"TILTAK_ELLER_VIRKEMIDDEL__LÆRLINGPLASS\"]")
    ),
    listOf(
        Contact("Vegard Veiledersen", "veileder@nav.no", "", "Veileder","Markedskontakt")
    ),
    classification
)

val enAd = enAdMed()

val enAdUtenKontaktinformasjon = Ad(
    "uuid",
    "annonsenr",
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
            "city",
            "country",
            "latitue",
            "longitude",
            "municipal_code",
            "county_code"
        )
    ),
    listOf(
        Property("sector", "Offentlig"),
        Property("adtext", "<h1>Tittel</h2><p>Den beste stillingen <b>noen sinne</b></p>"),
        Property("searchtags", "[{\"label\":\"Sales Promotion Manager\",\"score\":1.0},{\"label\":\"Salgssjef\",\"score\":0.25137392},{\"label\":\"Sales Manager (Hotels)\",\"score\":0.21487874},{\"label\":\"Promotions Director\",\"score\":0.09032349},{\"label\":\"Salgsfremmer\",\"score\":0.09004237}]"),
        Property("tags", "[\"INKLUDERING__ARBEIDSTID\", \"TILTAK_ELLER_VIRKEMIDDEL__LÆRLINGPLASS\"]")
    ),
    null,
    null
)

val enStillingsinfo = Stillingsinfo(
    "eierNavIdent",
    "eierNavn",
    "notat",
    enAd.getUuid(),
    "stillingsinfoid",
    "STILLING"
)
