package co.analisys.clase.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import co.analisys.clase.model.Clase;

public interface ClaseRepository extends JpaRepository<Clase, Long> {}
