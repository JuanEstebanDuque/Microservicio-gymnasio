package co.analisys.miembros.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import co.analisys.miembros.config.KafkaStreamsConfig;
import co.analisys.miembros.dto.DatosEntrenamiento;
import co.analisys.miembros.dto.ResumenEntrenamiento;
import co.analisys.miembros.dto.ResumenSemanal;

@Service
public class EntrenamientoService {

    private static final Duration VENTANA = Duration.ofDays(7);

    private final KafkaTemplate<String, DatosEntrenamiento> kafkaTemplate;
    private final StreamsBuilderFactoryBean streamsFactory;

    public EntrenamientoService(KafkaTemplate<String, DatosEntrenamiento> kafkaTemplate,
            StreamsBuilderFactoryBean streamsFactory) {
        this.kafkaTemplate = kafkaTemplate;
        this.streamsFactory = streamsFactory;
    }

    public void registrar(DatosEntrenamiento datos) {
        // La clave es el miembro: el stream agrupa por miembro
        kafkaTemplate.send(KafkaStreamsConfig.TOPIC_DATOS, String.valueOf(datos.miembroId()), datos);
    }

    /** Consulta el state store materializado por el stream processor. */
    public List<ResumenSemanal> resumenDelMiembro(Long miembroId) {
        KafkaStreams streams = streamsFactory.getKafkaStreams();
        if (streams == null) {
            throw new IllegalStateException("Kafka Streams aun no esta iniciado");
        }
        ReadOnlyWindowStore<String, ResumenEntrenamiento> store = streams.store(
                StoreQueryParameters.fromNameAndType(KafkaStreamsConfig.STORE_RESUMEN, QueryableStoreTypes.windowStore()));
        Instant ahora = Instant.now();
        List<ResumenSemanal> resultado = new ArrayList<>();
        try (WindowStoreIterator<ResumenEntrenamiento> it =
                store.fetch(String.valueOf(miembroId), ahora.minus(VENTANA), ahora)) {
            while (it.hasNext()) {
                KeyValue<Long, ResumenEntrenamiento> kv = it.next();
                Instant inicio = Instant.ofEpochMilli(kv.key);
                resultado.add(new ResumenSemanal(inicio, inicio.plus(VENTANA), kv.value));
            }
        }
        return resultado;
    }
}
