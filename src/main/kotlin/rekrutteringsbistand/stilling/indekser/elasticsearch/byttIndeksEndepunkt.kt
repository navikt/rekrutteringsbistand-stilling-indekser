package rekrutteringsbistand.stilling.indekser.elasticsearch

import io.javalin.http.Context
import rekrutteringsbistand.stilling.indekser.kafka.StillingConsumer
import rekrutteringsbistand.stilling.indekser.utils.log

fun byttIndeks(
    restContext: Context,
    gammelStillingConsumer: StillingConsumer?,
    elasticSearchService: ElasticSearchService
) {
    if (gammelStillingConsumer == null) {
        restContext
            .status(500)
            .result("Kan ikke bytte indeks, har ikke reindeksert")
    } else {
        gammelStillingConsumer.close()
        elasticSearchService.byttTilNyIndeks()

        val melding = "Reindeksering er ferdig, søk går nå mot ny indeks"
        log("byttIndeks()").info(melding)
        restContext
            .status(200)
            .result(melding)
    }
}
