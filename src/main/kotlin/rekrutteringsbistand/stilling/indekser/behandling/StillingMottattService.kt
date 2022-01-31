package rekrutteringsbistand.stilling.indekser.behandling

import no.nav.pam.stilling.ext.avro.Ad
import org.apache.http.ConnectionClosedException
import rekrutteringsbistand.stilling.indekser.elasticsearch.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.KunneIkkeHenteStillingsinsinfoException
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.log

class StillingMottattService(
    private val stillingsinfoClient: StillingsinfoClient,
    private val esService: ElasticSearchService
) {

    fun behandleStillingerMedRetry(ads: List<Ad>, indeksNavn: String) {
        try {
            behandleStillinger(ads, indeksNavn)
        } catch (exception: ConnectionClosedException) {
            log.warn("Feil ved kall mot Elastic Search, prøver igjen", exception)
            behandleStillinger(ads, indeksNavn)
        } catch (exception: KunneIkkeHenteStillingsinsinfoException) {
            log.warn("Feil ved henting av stillingsinfo, prøver igjen", exception)
            behandleStillinger(ads, indeksNavn)
        }
    }

    private fun behandleStillinger(ads: List<Ad>, indeksNavn: String) {
        val alleMeldinger = ads.map { konverterTilStilling(it) }
        val stillinger = beholdSisteMeldingPerStilling(alleMeldinger)
        val stillingsinfo = stillingsinfoClient.getStillingsinfo(stillinger.map { it.uuid })

        val rekrutteringsbistandStillinger = stillinger.map { stilling ->
            RekrutteringsbistandStilling(stilling, stillingsinfo.find { info -> info.stillingsid == stilling.uuid })
        }

        esService.indekser(rekrutteringsbistandStillinger, indeksNavn)
    }

    private fun beholdSisteMeldingPerStilling(stillinger: List<Stilling>): List<Stilling> {
        val sisteStillinger = hashMapOf<String, Stilling>()

        stillinger.forEach { stilling ->
            sisteStillinger[stilling.uuid] = stilling
        }

        return sisteStillinger.values.toList()
    }
}
