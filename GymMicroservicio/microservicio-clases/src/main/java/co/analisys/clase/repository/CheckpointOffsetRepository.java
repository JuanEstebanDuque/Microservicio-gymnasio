package co.analisys.clase.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.analisys.clase.model.CheckpointOffset;

public interface CheckpointOffsetRepository extends JpaRepository<CheckpointOffset, String> {
}
