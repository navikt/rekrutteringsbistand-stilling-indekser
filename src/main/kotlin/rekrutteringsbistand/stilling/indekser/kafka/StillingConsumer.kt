package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.lang.RuntimeException
import java.time.Duration

// TODO: Injecte KafkaConsumer her? Da kan den også brukes i konsumerTopicFraStart()
class StillingConsumer(private val stillingMottattService: StillingMottattService) {

    fun start() = GlobalScope.launch {
        KafkaConsumer<String, StillingDto>(consumerConfig).use { consumer ->
            consumer.subscribe(listOf("StillingEkstern"))

            while (true) {
                // Poll, behandle, repeat
                val records = consumer.poll(Duration.ofMillis(100))
                failHvisMerEnnEnRecord(records)
                stillingMottattService.behandleStilling(records.first().value())
            }
        }
    }

    private fun failHvisMerEnnEnRecord(records: ConsumerRecords<String, StillingDto>) {
        if (records.count() > 1) {
            throw RuntimeException("""
                Kafka konsumer er konfigurert til å kun motta én melding om gangen.
                Får vi flere meldinger om gangen kan vi miste meldinger pga. feil committa offset.
            """.trimIndent())
        }
    }

    fun konsumerTopicFraStart() {
        // TODO Implementer denne
        //  Brukes for reindeksering av hele indeks
//        val consumer = KafkaConsumer<String, StillingDto>(consumerConfig)
//        val partitions = consumer.partitionsFor("StillingEkstern")
//        consumer.seekToBeginning(listOf())
    }
}

data class StillingDto(
    val id: String,
)
