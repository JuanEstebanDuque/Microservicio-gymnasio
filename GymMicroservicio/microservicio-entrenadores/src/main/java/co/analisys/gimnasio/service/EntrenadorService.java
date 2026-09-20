package co.analisys.gimnasio.service;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.repository.EntrenadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntrenadorService {

    @Autowired
    private EntrenadorRepository entrenadorRepository;

    public Entrenador agregarEntrenador(Entrenador entrenador) {
        return entrenadorRepository.save(entrenador);
    }

    public List<Entrenador> obtenerTodosEntrenadores() {
        return entrenadorRepository.findAll();
    }

    public Optional<Entrenador> obtenerEntrenadorPorId(Long id) {
        return entrenadorRepository.findById(id);
    }

    public Entrenador actualizarEntrenador(Long id, Entrenador entrenadorActualizado) {
        return entrenadorRepository.findById(id)
                .map(entrenador -> {
                    entrenador.setNombre(entrenadorActualizado.getNombre());
                    entrenador.setEspecialidad(entrenadorActualizado.getEspecialidad());
                    return entrenadorRepository.save(entrenador);
                })
                .orElseThrow(() -> new RuntimeException("Entrenador no encontrado con id: " + id));
    }

    public void eliminarEntrenador(Long id) {
        entrenadorRepository.deleteById(id);
    }
}
