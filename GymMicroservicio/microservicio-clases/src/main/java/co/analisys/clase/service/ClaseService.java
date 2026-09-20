package co.analisys.clase.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.analisys.clase.client.EntrenadorClient;
import co.analisys.clase.exception.ClaseNoEncontradaException;
import co.analisys.clase.model.Clase;
import co.analisys.clase.repository.ClaseRepository;

@Service
public class ClaseService {

    private final ClaseRepository claseRepository;
    private final EntrenadorClient entrenadorClient;

    public ClaseService(ClaseRepository claseRepository, EntrenadorClient entrenadorClient) {
        this.claseRepository = claseRepository;
        this.entrenadorClient = entrenadorClient;
    }

    /**
     * Programa una nueva clase. Antes de persistir, valida contra el microservicio
     * de Entrenador que el entrenador asignado exista. Esta es la misma regla de
     * negocio que existía implícitamente en el monolito (una Clase siempre debía
     * tener un Entrenador válido asociado), ahora aplicada mediante comunicación
     * entre microservicios en lugar de una FK de base de datos.
     *
     * (microservicio-entrenadores no expone un concepto de "disponibilidad", solo
     * existencia del recurso, así que esa es la única validación posible aquí.)
     */
    public Clase programarClase(Clase clase) {
        entrenadorClient.validarEntrenador(clase.getEntrenadorId());
        return claseRepository.save(clase);
    }

    public List<Clase> obtenerTodasLasClases() {
        return claseRepository.findAll();
    }

    public Clase obtenerClasePorId(Long id) {
        return claseRepository.findById(id)
                .orElseThrow(() -> new ClaseNoEncontradaException(id));
    }
}
