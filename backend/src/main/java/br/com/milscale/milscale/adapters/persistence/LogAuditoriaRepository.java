package br.com.milscale.milscale.adapters.persistence;

import br.com.milscale.milscale.domain.LogAuditoria;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findAllByOrderByDataHoraDesc(Pageable pageable);
}
