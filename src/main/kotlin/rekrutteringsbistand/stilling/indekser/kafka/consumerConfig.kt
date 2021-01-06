package rekrutteringsbistand.stilling.indekser.kafka

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import rekrutteringsbistand.stilling.indekser.environment
import java.io.File
import java.lang.RuntimeException
import java.util.*

val consumerConfig = Properties().apply {
    put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1)
    put(ConsumerConfig.GROUP_ID_CONFIG, "rekrutteringsbistand-stilling-indekser")
    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java)
    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer::class.java)

    put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT")
    put(SaslConfigs.SASL_MECHANISM, "PLAIN")
    put(SaslConfigs.SASL_JAAS_CONFIG, saslJaasConfig)

    System.getenv("NAV_TRUSTSTORE_PATH")?.let {
        put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL")
        put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, File(it).absolutePath)
        put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, System.getenv("NAV_TRUSTSTORE_PASSWORD"))
    }

    put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://kafka-schema-registry.tpa.svc.nais.local:8081")
}

private val bootstrapServers = when (environment().get("NAIS_CLUSTER_NAME")) {
    "dev-gcp" -> "b27apvl00045.preprod.local:8443, b27apvl00046.preprod.local:8443, b27apvl00047.preprod.local:8443"
    "prod-gcp" -> "a01apvl00145.adeo.no:8443, a01apvl00146.adeo.no:8443, a01apvl00147.adeo.no:8443, a01apvl00148.adeo.no:8443, a01apvl00149.adeo.no:8443, a01apvl00150.adeo.no:8443"
    else -> throw RuntimeException("Kunne ikke hente cluster-name")
}

private val serviceuserUsername: String = environment().get("SERVICEBRUKER_BRUKERNAVN")
private val serviceuserPassword: String = environment().get("SERVICEBRUKER_PASSORD")
private val saslJaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"${serviceuserUsername}\" password=\"${serviceuserPassword}\";"
