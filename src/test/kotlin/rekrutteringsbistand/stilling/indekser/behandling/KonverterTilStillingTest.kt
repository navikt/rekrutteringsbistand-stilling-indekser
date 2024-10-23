package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Classification
import no.nav.pam.stilling.ext.avro.Contact
import no.nav.pam.stilling.ext.avro.StyrkCategory
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enAdMed
import rekrutteringsbistand.stilling.indekser.setup.enAdUtenKontaktinformasjon
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KonverterTilStillingTest {
    // Koden som har kjørt i prod ville ha kastet null-pointer exception om categories er null. Vi har derfor
    // ikke skrevet noen nye unit-tester for hva vi skal gjøre i en slik hypotetisk situasjon.

    val kassemedarbeider4siffer = StyrkCategory("5223", "Butikkmedarbeider")
    val kassemedarbeider6siffer = StyrkCategory("5223.19", "Kassemedarbeider (butikk)")

    val kranfører4siffer = StyrkCategory("8343", "Kranfører")
    val kranfører6siffer = StyrkCategory("8343.05", "Byggekranfører")


    // Gitt en annonse for en direktemeldt stilling med flere styrk-koder
    // når konverterer
    // så skal styrkEllerTittel-feltet være styrknavnet til styrk-oden med 6 siffer (fordi det er bare Rekbis som bruker 6 siffer)
    @Test
    fun `Skal mappe JANZZ-navn til tittel for direktemeldt stilling`() {
        val styrk = listOf(kassemedarbeider4siffer, kassemedarbeider6siffer)
        val tittelFraArbeidsplassen = "Tittel fra arbeidsplassen"

        val janzzkategori = Classification(
            "JANZZ",
            "126666",
            "Tittel fra arbeidsplassen",
            1.0,
            ""
        )
        val resultat = konverterTilStilling(
            enAdMed(
                source = "DIR",
                categories = styrk,
                title = tittelFraArbeidsplassen,
                classification = listOf(janzzkategori)
            )
        )

        assertEquals(kassemedarbeider6siffer.getName(), resultat.styrkEllerTittel)
        assertEquals(janzzkategori.getName(), resultat.tittel)
        assertEquals(tittelFraArbeidsplassen, resultat.title) // NB: byttet ut med null-assert når migrering er ferdig
        assertEquals("DIR", resultat.source)
    }

    // Gitt en annonse for en ekstern stilling med flere styrk-koder
    // når konverterer
    // så skal tittelfeltet være arbeidsplassen-tittelen
    @Test
    fun `Skal mappe arbeidsplassentittel for ekstern stilling`() {
        val tittelFraArbeidsplassen = "Tittel fra arbeidsplassen"
        val styrk = listOf(kassemedarbeider6siffer, kassemedarbeider4siffer)

        val resultat = konverterTilStilling(
            enAdMed(
                source = "ekstern",
                categories = styrk,
                title = tittelFraArbeidsplassen
            )
        )

        assertEquals(tittelFraArbeidsplassen, resultat.styrkEllerTittel)
        assertEquals(tittelFraArbeidsplassen, resultat.title)  // NB: byttet ut med null-assert når migrering er ferdig
        assertEquals("ekstern", resultat.source)
    }


    // Gitt en annonse for en direktemeldt stilling med flere gyldige styrk-koder med seks siffer
    // når konverterer
    // så skal vi flette sammen styrkene med bindestrek
    @Test
    fun `Skal kaste feil dersom vi har flere gyldige styrk koder med seks siffer for intern stilling`() {
        val styrk = listOf(kassemedarbeider6siffer, kranfører6siffer, kranfører4siffer)

        val stilling1 = konverterTilStilling(enAdMed(source = "DIR", categories = styrk))

        assertEquals("Byggekranfører/Kassemedarbeider (butikk)", stilling1.styrkEllerTittel)
    }


    // Gitt en annonse for en direktemeldt stilling med janzz-kode
    // når konverterer
    // så skal jenzTittel bli janzz-kategorien
    @Test
    fun `Skal returnere janzzkategori om tilgjengelig`() {
        val stilling1 = konverterTilStilling(enAdMed(classification = listOf(
            Classification("JANZZ", "1234", "Feil tittel", 0.1, "123"),
            Classification("JANZZ", "1234", "Løvetemmer", 1.0, "123"),
            Classification("JANZZ", "1234", "Feil tittel", 0.99, "123")
        ), source = "DIR"))

        assertEquals("Løvetemmer", stilling1.tittel)
    }

    // Gitt en annonse for en direktemeldt stilling uten styrk
    // når konverterer
    // så skal ???
    @Test
    fun `Skal kaste feil dersom vi ikke har styrk koder for intern stilling`() {
        val stilling1 = konverterTilStilling(enAdMed(source = "DIR", categories = listOf()))

        assertEquals("Stilling uten valgt jobbtittel", stilling1.styrkEllerTittel)
        assertEquals("Stilling uten valgt jobbtittel", stilling1.tittel)

    }

    // Gitt en annonse for en direktemeldt stilling med kun 4-sifret styrkkode
    // når konverterer
    // så skal ???
    @Test
    fun `Skal kaste feil dersom vi kun har 4-sifret styrk koder for intern stilling`() {
        val stilling1 = konverterTilStilling(enAdMed(source = "DIR", categories = listOf(kranfører4siffer)))
        assertEquals("Stilling uten valgt jobbtittel", stilling1.styrkEllerTittel)
        assertEquals("Stilling uten valgt jobbtittel", stilling1.tittel)
    }


    // Gitt en annonse for en direktemeldt stilling med styrk som har feil format
    // når konverterer
    // så skal tittelfeltet inneholde en standardtekst (TODO: hva?)
    @Test
    fun `Skal kaste feil dersom en direktemeldt stilling har kun feil format på styrk kode`() {
        val stilling1 =
            konverterTilStilling(enAdMed(source = "DIR", categories = listOf(StyrkCategory("000.000", "FEIL FORMAT"))))
        assertEquals("Stilling uten valgt jobbtittel", stilling1.styrkEllerTittel)
        assertEquals("Stilling uten valgt jobbtittel", stilling1.tittel)
        val stilling2 =
            konverterTilStilling(enAdMed(source = "DIR", categories = listOf(StyrkCategory("000000", "FEIL FORMAT"))))
        assertEquals("Stilling uten valgt jobbtittel", stilling1.styrkEllerTittel)
        assertEquals("Stilling uten valgt jobbtittel", stilling1.tittel)

    }

    @Test
    fun `Skal mappe felter riktig`() {
        val resultat = konverterTilStilling(enAd)
        assertEquals(enAd.getPublishedByAdmin(), resultat.publishedByAdmin)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(enAd.getCreated(), resultat.created)
        assertEquals(enAd.getPublished(), resultat.published)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(
            resultat.properties["tags"],
            jacksonObjectMapper().readTree("[\"INKLUDERING__ARBEIDSTID\", \"TILTAK_ELLER_VIRKEMIDDEL__LÆRLINGPLASS\"]")
        )
        assertEqualContactLists(enAd.getContacts(), resultat.contacts)
    }

    @Test
    fun `Skal mappe stilling uten kontaktinformasjon`() {
        val resultat = konverterTilStilling(enAdUtenKontaktinformasjon)
        assertTrue(resultat.contacts.isEmpty())
    }
}

fun assertEqualContactLists(
    adContactList: List<Contact>,
    stillingContactList: List<rekrutteringsbistand.stilling.indekser.opensearch.Contact>
) {
    assertEquals(adContactList.size, stillingContactList.size)
    adContactList.forEachIndexed { index, adContact ->
        assertEquals(adContact.getName(), stillingContactList[index].name)
        assertEquals(adContact.getRole(), stillingContactList[index].role)
        assertEquals(adContact.getTitle(), stillingContactList[index].title)
        assertEquals(adContact.getEmail(), stillingContactList[index].email)
        assertEquals(adContact.getPhone(), stillingContactList[index].phone)
    }
}
