package co.analisys.clase.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

/** Registro persistido de cada actualizacion de ocupacion procesada por el proceso de recuperacion. */
@Data
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "kafkaPartition", "kafkaOffset" }))
public class OcupacionHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long claseId;
    private int ocupacionActual;
    private LocalDateTime momento;
    private int kafkaPartition;
    private long kafkaOffset;
}
