package co.analisys.miembros.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import co.analisys.miembros.model.Miembro;

public interface MiembroRepository extends JpaRepository<Miembro, Long> {
    Optional<Miembro> findByEmail(String email);

    //@Query("SELECT m FROM Miembro m WHERE m.email = ?1 AND m.password = ?2")
    //Optional<Miembro> findByEmailAndPassword(String email, String password);
}
