package co.analisys.miembros.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.analisys.miembros.model.Miembro;
import co.analisys.miembros.repository.MiembroRepository;

@Service
public class MiembroService {

    private final MiembroRepository miembroRepository;

    public MiembroService(MiembroRepository miembroRepository) {
        this.miembroRepository = miembroRepository;
    }

    public Miembro registrarMiembro(Miembro miembro) {
        return miembroRepository.save(miembro);
    }

    public List<Miembro> obtenerTodosLosMiembros() {
        return miembroRepository.findAll();
    }

}