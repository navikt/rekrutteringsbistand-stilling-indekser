package rekrutteringsbistand.stilling.indekser.kafka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.pam.ad.ext.avro.Ad
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import rekrutteringsbistand.stilling.indekser.behandling.StillingMottattService
import rekrutteringsbistand.stilling.indekser.utils.log
import java.time.Duration
import java.time.LocalDateTime

class StillingConsumerImpl(
    private val kafkaConsumer: KafkaConsumer<String, Ad>,
    private val stillingMottattService: StillingMottattService
): StillingConsumer {

    override suspend fun start(indeksNavn: String) = withContext(Dispatchers.IO) {
            try {
                kafkaConsumer.subscribe(listOf("StillingEkstern"))

                log.info("Starter å konsumere StillingEkstern-topic med groupId ${kafkaConsumer.groupMetadata().groupId()}, " +
                        "indekserer på indeks '$indeksNavn'")

                var før = LocalDateTime.now()
                while (true) {
                    val records: ConsumerRecords<String, Ad> = kafkaConsumer.poll(Duration.ofSeconds(30))
                    if (records.count() == 0) continue

                    val stillinger = records.map { it.value() }
                    stillingMottattService.behandleStillinger(stillinger, indeksNavn)
                    kafkaConsumer.commitSync()
                    log.info("Committet offset ${records.last().offset()} til Kafka")

                    val diff = Duration.between(før, LocalDateTime.now()).toMillis()
                    før = LocalDateTime.now()
                    log.info("Konsumering av ${records.count()} stillinger tok $diff ms. I snitt ${diff / records.count()} ms per stilling")
                }

                // TODO: Retry-mekanismer

            } catch (exception: WakeupException) {
                log.info("Fikk beskjed om å lukke consument med groupId ${kafkaConsumer.groupMetadata().groupId()}")
            } finally {
                kafkaConsumer.close()
            }
    }

    override fun stopp() {
        kafkaConsumer.wakeup()
    }
}

interface StillingConsumer {
    suspend fun start(indeksNavn: String)
    fun stopp()
}
