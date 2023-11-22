package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pam.stilling.ext.avro.Contact
import no.nav.pam.stilling.ext.avro.StyrkCategory
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.setup.enAd
import rekrutteringsbistand.stilling.indekser.setup.enAdMed
import rekrutteringsbistand.stilling.indekser.setup.enAdUtenKontaktinformasjon
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class KonverterTilStillingTest {
    // Koden i dag antar at categories ikke er null, og vi har ikke sett noen exceptions i prod,
    // sa unit-tester ikke adferd for det her

    val styrk08Kassemedarbeider4siffer = StyrkCategory("5223", "Butikkmedarbeider")
    val styrk08Kassemedarbeider6siffer = StyrkCategory("5223.19", "Kassemedarbeider (butikk)")

    val styrk08Kranfører4siffer = StyrkCategory("8343", "Kranfører")
    val styrk08Kranfører6siffer = StyrkCategory("8343.05", "Byggekranfører")


    // Gitt en annonse for en direktemeldt stilling med flere styrk-koder
    // når konverterer
    // så skal styrkEllerTittel-feltet være styrknavnet til styrk-oden med 6 siffer (fordi det er bare Rekbis som bruker 6 siffer)
    @Test
    fun `Skal mappe STYRK-navn til tittel for direktemeldt stilling`() {
        val styrk = listOf(styrk08Kassemedarbeider4siffer, styrk08Kassemedarbeider6siffer)

        val tittelFraArbeidsplassen = "Tittel fra arbeidsplassen"
        val resultat = konverterTilStilling(enAdMed(
            source = "DIR",
            categories = styrk,
            title = tittelFraArbeidsplassen
        ))

        assertEquals(styrk08Kassemedarbeider6siffer.getName(), resultat.styrkEllerTittel)
        assertEquals(tittelFraArbeidsplassen, resultat.title) // NB: assert byttet ut med null-ish når migrering er ferdig
        assertEquals("DIR", resultat.source)
    }

    // Gitt en annonse for en ekstern stilling med flere styrk-koder
    // når konverterer
    // så skal tittelfeltet være arbeidsplassen-tittelen
    @Test
    fun `Skal mappe arbeidsplassentittel for ekstern stilling`() {
        val tittelFraArbeidsplassen = "Tittel fra arbeidsplassen"
        val styrk = listOf(styrk08Kassemedarbeider6siffer, styrk08Kassemedarbeider4siffer)

        val resultat = konverterTilStilling(
            enAdMed(
                source = "ekstern",
                categories = styrk,
                title = tittelFraArbeidsplassen
            ))

        assertEquals(tittelFraArbeidsplassen, resultat.styrkEllerTittel)
        assertEquals(tittelFraArbeidsplassen, resultat.title)
        assertEquals("ekstern", resultat.source)
    }


    // Gitt en annonse for en direktemeldt stilling med flere gyldige styrk-koder med seks siffer
    // når konverterer
    // så skal vi kast exception
    @Test
    fun `Skal kaste feil dersom vi har flere gyldige styrk koder med seks siffer for intern stilling`() {
        val styrk = listOf(styrk08Kassemedarbeider6siffer, styrk08Kranfører6siffer, styrk08Kranfører4siffer)

        assertFailsWith(RuntimeException::class) {
            konverterTilStilling(enAdMed(source = "DIR", categories = styrk))
        }
    }


    // Gitt en annonse for en direktemeldt stilling uten styrk
    // når konverterer
    // så skal ???
    @Test
    fun `Skal kaste feil dersom vi ikke har styrk koder for intern stilling`() {
        assertFailsWith(RuntimeException::class) {
            konverterTilStilling(enAdMed(source = "DIR", categories = listOf()))
        }
    }

    // Gitt en annonse for en direktemeldt stilling med kun 4-sifret styrkkode
    // når konverterer
    // så skal ???
    @Test
    fun `Skal kaste feil dersom vi kun har 4-sifret styrk koder for intern stilling`() {
        assertFailsWith(RuntimeException::class) {
            konverterTilStilling(enAdMed(source = "DIR", categories = listOf(styrk08Kranfører4siffer)))
        }
    }


    // Gitt en annonse for en direktemeldt stilling med styrk som har feil format
    // når konverterer
    // så skal tittelfeltet inneholde en standardtekst (TODO: hva?)

    // Denne venter vi med til altt annet er gjort
    // Gitt en annonse for en direktemeldt stilling, som selvfølgelig har tittel
    // når konverterer
    // så skal ikke tittel finnes


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

fun assertEqualContactLists(adContactList: List<Contact>, stillingContactList: List<rekrutteringsbistand.stilling.indekser.opensearch.Contact>) {
    assertEquals(adContactList.size, stillingContactList.size)
    adContactList.forEachIndexed { index, adContact ->
        assertEquals(adContact.getName(), stillingContactList[index].name)
        assertEquals(adContact.getRole(), stillingContactList[index].role)
        assertEquals(adContact.getTitle(), stillingContactList[index].title)
        assertEquals(adContact.getEmail(), stillingContactList[index].email)
        assertEquals(adContact.getPhone(), stillingContactList[index].phone)
    }
}
