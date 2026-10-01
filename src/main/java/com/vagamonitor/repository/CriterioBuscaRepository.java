package com.vagamonitor.repository;

import com.vagamonitor.model.CriterioBusca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CriterioBuscaRepository extends JpaRepository<CriterioBusca, Long> {
}
