package co.analisys.clase.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.analisys.clase.model.OcupacionHistorial;

public interface OcupacionHistorialRepository extends JpaRepository<OcupacionHistorial, Long> {

    List<OcupacionHistorial> findTop50ByOrderByIdDesc();
}
