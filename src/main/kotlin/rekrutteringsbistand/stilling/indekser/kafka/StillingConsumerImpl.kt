package rekrutteringsbistand.stilling.indekser.kafka

import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration

// TODO: Injecte KafkaConsumer her? Da kan den også brukes i konsumerTopicFraStart()
class StillingConsumerImpl(private val stillingMottattService: StillingMottattService): StillingConsumer {

    override fun start() {
        KafkaConsumer<String, Ad>(consumerConfig()).use { consumer ->
            consumer.subscribe(listOf("StillingEkstern"))

            var counter = 0
            while (true) {
                counter += 1
                val records: ConsumerRecords<String, Ad> = consumer.poll(Duration.ofSeconds(30))
                failHvisMerEnnEnRecord(records)
                if (records.count() == 0) continue
                val melding = records.first()
                stillingMottattService.behandleStilling(melding.value(), counter)
                consumer.commitSync()
            }

            // TODO: Behandle melding
            // TODO: Retry-mekanismer
        }
    }

    private fun failHvisMerEnnEnRecord(records: ConsumerRecords<String, Ad>) {
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

interface StillingConsumer {
    fun start()
}
