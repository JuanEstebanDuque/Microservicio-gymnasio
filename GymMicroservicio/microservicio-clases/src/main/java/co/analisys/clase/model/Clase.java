package co.analisys.clase.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Aggregate root del Bounded Context "Gestión de Clases".
 *
 * En el monolito, Clase tenía una relación @ManyToOne hacia la entidad Entrenador
 * (mapeada por JPA, misma base de datos). Al separar Entrenador en su propio
 * microservicio (su propio Bounded Context y su propia base de datos), esa relación
 * ya no puede modelarse como una FK/JOIN de JPA: cada microservicio es dueño exclusivo
 * de sus datos.
 *
 * Por eso aquí el entrenador se referencia únicamente por su identificador
 * (entrenadorId), tratado como una referencia externa. La validez de ese id
 * (que el entrenador exista) se verifica en tiempo de negocio llamando al
 * microservicio de Entrenador vía REST (ver EntrenadorClient), no mediante
 * una relación de base de datos.
 */
@Data
@Entity
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private LocalDateTime horario;

    private int capacidadMaxima;

    /**
     * Referencia al entrenador asignado, resuelto por el microservicio de Entrenador.
     * Se guarda solo el id (no un objeto Entrenador) para no acoplar la base de datos
     * de Clase con la de Entrenador.
     */
    private Long entrenadorId;
}
