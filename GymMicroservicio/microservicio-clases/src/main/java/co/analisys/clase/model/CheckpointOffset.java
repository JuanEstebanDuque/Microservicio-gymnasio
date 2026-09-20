package co.analisys.clase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/** Ultimo offset procesado por topic y particion; se guarda en la misma transaccion que el resultado. */
@Data
@Entity
public class CheckpointOffset {

    /** Formato topic-particion, p. ej. ocupacion-clases-0 */
    @Id
    private String id;

    private String topic;
    private int kafkaPartition;
    private long ultimoOffset;
}
