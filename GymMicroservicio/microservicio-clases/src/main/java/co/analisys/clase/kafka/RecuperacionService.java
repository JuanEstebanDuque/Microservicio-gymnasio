package co.analisys.clase.kafka;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import co.analisys.clase.config.KafkaConfig;
import co.analisys.clase.dto.OcupacionClase;
import jakarta.annotation.PreDestroy;
import tools.jackson.databind.json.JsonMapper;

/**
 * Proceso de recuperacion: lee el log de Kafka (retenido 7 dias) sin depender de los offsets del
 * broker. Al arrancar carga el ultimo checkpoint de la base de datos y hace seek al siguiente
 * offset; si no hay checkpoint, reprocesa desde el inicio del log.
 */
@Service
public class RecuperacionService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionService.class);

    private final CheckpointService checkpointService;
    private final JsonMapper jsonMapper;
    private final KafkaProperties kafkaProperties;
    private volatile KafkaConsumer<String, String> consumer;
    private volatile boolean activo = true;

    public RecuperacionService(CheckpointService checkpointService, JsonMapper jsonMapper,
            KafkaProperties kafkaProperties) {
        this.checkpointService = checkpointService;
        this.jsonMapper = jsonMapper;
        this.kafkaProperties = kafkaProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void iniciar() {
        Thread hilo = new Thread(this::iniciarProcesamiento, "recuperacion-kafka");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void iniciarProcesamiento() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "recuperacion-grupo");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // el checkpoint vive en la BD
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> kc = new KafkaConsumer<>(props)) {
            this.consumer = kc;
            List<TopicPartition> particiones = esperarParticiones(kc);
            kc.assign(particiones);

            for (TopicPartition tp : particiones) {
                Long ultimo = checkpointService.cargarUltimoOffset(tp.topic(), tp.partition());
                if (ultimo != null) {
                    kc.seek(tp, ultimo + 1);
                    log.info("[RECUPERACION] {} retoma desde el checkpoint: offset {}", tp, ultimo + 1);
                } else {
                    kc.seekToBeginning(List.of(tp));
                    log.info("[RECUPERACION] {} sin checkpoint: reprocesa desde el inicio del log", tp);
                }
            }

            while (activo) {
                ConsumerRecords<String, String> records = kc.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    procesarRecord(record);
                }
            }
        } catch (WakeupException e) {
            log.info("[RECUPERACION] Detenido");
        } catch (Exception e) {
            log.error("[RECUPERACION] Fallo el proceso de recuperacion", e);
        }
    }

    private List<TopicPartition> esperarParticiones(KafkaConsumer<String, String> kc) throws InterruptedException {
        for (int i = 0; i < 60 && activo; i++) {
            List<PartitionInfo> info = kc.listTopics().get(KafkaConfig.TOPIC_OCUPACION);
            if (info != null && !info.isEmpty()) {
                return info.stream().map(p -> new TopicPartition(p.topic(), p.partition())).toList();
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("El topic " + KafkaConfig.TOPIC_OCUPACION + " no existe");
    }

    private void procesarRecord(ConsumerRecord<String, String> record) {
        OcupacionClase ocupacion = jsonMapper.readValue(record.value(), OcupacionClase.class);
        checkpointService.procesarYGuardar(record.topic(), record.partition(), record.offset(), ocupacion);
        log.info("[RECUPERACION] Procesado {}-{} offset {} (clase {}, ocupacion {})",
                record.topic(), record.partition(), record.offset(), ocupacion.claseId(), ocupacion.ocupacionActual());
    }

    @PreDestroy
    public void detener() {
        activo = false;
        KafkaConsumer<String, String> kc = consumer;
        if (kc != null) {
            kc.wakeup();
        }
    }
}
