package co.analisys.miembros.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.analisys.miembros.messaging.InscripcionPublisher;
import co.analisys.miembros.model.Miembro;
import co.analisys.miembros.repository.MiembroRepository;

@Service
public class MiembroService {

    private final MiembroRepository miembroRepository;

    private final InscripcionPublisher inscripcionPublisher;

    public MiembroService(MiembroRepository miembroRepository, InscripcionPublisher inscripcionPublisher) {
        this.miembroRepository = miembroRepository;
        this.inscripcionPublisher = inscripcionPublisher;
    }

    public Miembro registrarMiembro(Miembro miembro) {
        Miembro guardado = miembroRepository.save(miembro);
        inscripcionPublisher.publicar(guardado);
        return guardado;
    }

    public List<Miembro> obtenerTodosLosMiembros() {
        return miembroRepository.findAll();
    }

}