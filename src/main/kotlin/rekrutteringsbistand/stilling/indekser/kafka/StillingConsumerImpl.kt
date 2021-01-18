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
import kotlin.system.measureTimeMillis

class StillingConsumerImpl(
    private val kafkaConsumer: KafkaConsumer<String, Ad>,
    private val stillingMottattService: StillingMottattService
): StillingConsumer {

    override suspend fun start(indeksNavn: String) = withContext(Dispatchers.IO) {
            try {
                kafkaConsumer.subscribe(listOf("StillingEkstern"))

                log.info("Starter å konsumere StillingEkstern-topic med groupId ${kafkaConsumer.groupMetadata().groupId()}, " +
                        "indekserer på indeks '$indeksNavn'")

                var counter = 0
                var før = LocalDateTime.now()

                while (true) {
                    val records: ConsumerRecords<String, Ad> = kafkaConsumer.poll(Duration.ofSeconds(30))
                    failHvisMerEnnEnRecord(records)
                    if (records.count() == 0) continue
                    val melding = records.first()
                    stillingMottattService.behandleStilling(melding.value(), indeksNavn)
                    kafkaConsumer.commitSync()
                    log.info("Committet offset ${melding.offset()} til Kafka")

                    if (counter++ % 100 == 0) {
                        val diff = Duration.between(før, LocalDateTime.now()).toMillis()
                        før = LocalDateTime.now()

                        log.info("Konsumering av 100 stillinger tok $diff ms. I snitt ${diff / 100} ms per stilling")
                    }
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

    private fun failHvisMerEnnEnRecord(records: ConsumerRecords<String, Ad>) {
        if (records.count() > 1) {
            throw Exception("""
                Kafka konsumer er konfigurert til å kun motta én melding om gangen.
                Får vi flere meldinger om gangen kan vi miste meldinger pga. feil committa offset.
            """.trimIndent())
        }
    }
}

interface StillingConsumer {
    suspend fun start(indeksNavn: String)
    fun stopp()
}
