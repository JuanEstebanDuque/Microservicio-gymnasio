package co.analisys.equipo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import co.analisys.equipo.model.Equipo;
import co.analisys.equipo.repository.EquipoRepository;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;

    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    public Equipo agregarEquipo(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    public List<Equipo> obtenerTodosLosEquipos() {
        return equipoRepository.findAll();
    } 

}