package co.analisys.miembros.config;

import java.time.Duration;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.serializer.JacksonJsonSerde;

import co.analisys.miembros.dto.DatosEntrenamiento;
import co.analisys.miembros.dto.ResumenEntrenamiento;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    public static final String TOPIC_DATOS = "datos-entrenamiento";
    public static final String TOPIC_RESUMEN = "resumen-entrenamiento";
    public static final String STORE_RESUMEN = "resumen-entrenamiento-store";

    @Bean
    public NewTopic datosEntrenamientoTopic() {
        return TopicBuilder.name(TOPIC_DATOS).partitions(1).replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7L * 24 * 60 * 60 * 1000)).build();
    }

    @Bean
    public NewTopic resumenEntrenamientoTopic() {
        return TopicBuilder.name(TOPIC_RESUMEN).partitions(1).replicas(1).build();
    }

    /** Agrega por miembro, en ventanas de 7 dias: sesiones, minutos y calorias. */
    @Bean
    public KStream<String, DatosEntrenamiento> kStream(StreamsBuilder streamsBuilder) {
        JacksonJsonSerde<DatosEntrenamiento> datosSerde = new JacksonJsonSerde<>(DatosEntrenamiento.class);
        JacksonJsonSerde<ResumenEntrenamiento> resumenSerde = new JacksonJsonSerde<>(ResumenEntrenamiento.class);

        KStream<String, DatosEntrenamiento> stream = streamsBuilder.stream(
                TOPIC_DATOS, Consumed.with(Serdes.String(), datosSerde));

        stream.groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofDays(7)))
                .aggregate(
                        ResumenEntrenamiento::new,
                        (key, value, aggregate) -> aggregate.actualizar(value),
                        Materialized.<String, ResumenEntrenamiento, WindowStore<Bytes, byte[]>>as(STORE_RESUMEN)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(resumenSerde))
                .toStream()
                .map((ventana, resumen) -> KeyValue.pair(ventana.key(), resumen))
                .to(TOPIC_RESUMEN, Produced.with(Serdes.String(), resumenSerde));
        return stream;
    }
}
