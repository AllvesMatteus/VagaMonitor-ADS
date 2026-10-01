package com.vagamonitor.repository;

import com.vagamonitor.model.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long> {
    Optional<Vaga> findByIdExterno(String idExterno);
    boolean existsByIdExterno(String idExterno);
}
