package rekrutteringsbistand.stilling.indekser.behandling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import rekrutteringsbistand.stilling.indekser.setup.enAd
import kotlin.test.assertEquals

class KonverterTilStillingTest {

    @Test
    fun `Skal mappe felter riktig`() {
        val resultat = konverterTilStilling(enAd)
        assertEquals(enAd.getPublishedByAdmin(), resultat.publishedByAdmin)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(enAd.getCreated(), resultat.created)
        assertEquals(enAd.getPublished(), resultat.published)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(enAd.getExpires(), resultat.expires)
        assertEquals(
            resultat.properties["tags"],
            jacksonObjectMapper().readTree("[\"INKLUDERING__ARBEIDSTID\", \"TILTAK_ELLER_VIRKEMIDDEL__LÆRLINGPLASS\"]")
        )
    }
}