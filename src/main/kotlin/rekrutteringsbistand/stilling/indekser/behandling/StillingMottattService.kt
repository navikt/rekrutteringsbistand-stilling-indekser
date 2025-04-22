package rekrutteringsbistand.stilling.indekser.behandling

import no.nav.pam.stilling.ext.avro.Ad
import org.apache.http.ConnectionClosedException
import rekrutteringsbistand.stilling.indekser.opensearch.*
import rekrutteringsbistand.stilling.indekser.stillingsinfo.KunneIkkeHenteStillingsinsinfoException
import rekrutteringsbistand.stilling.indekser.stillingsinfo.StillingsinfoClient
import rekrutteringsbistand.stilling.indekser.utils.log

class StillingMottattService(
    private val stillingsinfoClient: StillingsinfoClient,
    private val osService: OpenSearchService
) {

    fun behandleStillingerMedRetry(ads: List<Ad>, indeksNavn: String) {
        try {
            behandleStillinger(ads, indeksNavn)
        } catch (exception: ConnectionClosedException) {
            log.warn("Feil ved kall mot Open Search, prøver igjen", exception)
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

        // Sender stillingene til stilling-api for lagring, stoppet midlertidig
        //sendDIRStillingtilStillingsApi(rekrutteringsbistandStillinger)

        osService.indekser(rekrutteringsbistandStillinger, indeksNavn)
    }

    private fun sendDIRStillingtilStillingsApi(stillinger: List<RekrutteringsbistandStilling>) {
        stillinger.forEach { stilling ->
            if(stilling.stilling.source == "DIR") (
                stillingsinfoClient.sendStillingsId(stilling.stilling.uuid)
            )
        }
    }

    private fun beholdSisteMeldingPerStilling(stillinger: List<Stilling>) =
        stillinger.associateBy { it.uuid }.values.toList()
}
