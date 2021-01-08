package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import rekrutteringsbistand.stilling.indekser.log
import java.time.Duration

// TODO: Injecte KafkaConsumer her? Da kan den også brukes i konsumerTopicFraStart()
class StillingConsumerImpl(private val stillingMottattService: StillingMottattService): StillingConsumer {

    override fun start() {
        KafkaConsumer<String, Ad>(consumerConfig()).use { consumer ->
            consumer.subscribe(listOf("StillingEkstern"))

            val records: ConsumerRecords<String, Ad> = consumer.poll(Duration.ofSeconds(30))
            if (records.count() > 0) {
                log.info("Mottok melding på Kafka: ${records.first()}")
            } else {
                log.info("Fikk ikke noen meldinger")
            }
            // TODO: Behandle melding
            // TODO: Retry-mekanismer
        }
    }

    private fun failHvisMerEnnEnRecord(records: ConsumerRecords<String, StillingDto>) {
        if (records.count() > 1) {
            throw Exception("""
                Kafka konsumer er konfigurert til å kun motta én melding om gangen.
                Får vi flere meldinger om gangen kan vi miste meldinger pga. feil committa offset.
            """.trimIndent())
        }
    }

    fun konsumerTopicFraStart() {
        // TODO Implementer denne
        //  Brukes for reindeksering av hele indeks
    }
}

data class StillingDto(
    val id: String,
)

interface StillingConsumer {
    fun start()
}
