package co.analisys.equipo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.analisys.equipo.model.Equipo;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {}