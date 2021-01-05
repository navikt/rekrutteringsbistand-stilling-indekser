package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.KafkaConsumer

class StillingConsumer(
) {
    fun start() {
        GlobalScope.launch {
            KafkaConsumer<String, StillingDto>(consumerConfig).use { consumer ->
                consumer.subscribe(listOf("StillingEkstern"))

                while (true) {
                    // Poll, behandle, repeat
                }
            }
        }
    }
}

data class StillingDto(
        val id: String,
)