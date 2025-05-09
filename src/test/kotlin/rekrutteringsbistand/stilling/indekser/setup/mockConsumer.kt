package rekrutteringsbistand.stilling.indekser.setup

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nav.pam.stilling.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.TopicPartition
import rekrutteringsbistand.stilling.indekser.kafka.stillingstopic

val topic = TopicPartition(stillingstopic, 0)

fun mockConsumer(periodiskSendMeldinger: Boolean = true) =
    MockConsumer<String, Ad>(OffsetResetStrategy.EARLIEST).apply {
        schedulePollTask {
            rebalance(listOf(topic))
            updateBeginningOffsets(mapOf(Pair(topic, 0)))

            if (periodiskSendMeldinger) {
                @OptIn(DelicateCoroutinesApi::class) // This coroutine should be active for the whole duration of the application's lifetimeo
                GlobalScope.launch {
                    var offset: Long = 0
                    while (!closed()) {
                        addRecord(ConsumerRecord(stillingstopic, 0, offset++, enAd.getUuid(), enAd))
                        delay(5_000)
                    }
                }
            }
        }
    }
