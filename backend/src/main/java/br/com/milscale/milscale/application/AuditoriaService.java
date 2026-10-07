package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.LogAuditoriaRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.LogAuditoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditoriaService {
    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository, UsuarioRepository usuarioRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(String login, String acao, String descricao) {
        try {
            String nomeExibicao = login == null ? "sistema" : usuarioRepository.findByLogin(login)
                    .map(u -> u.getMilitar().getNomeExibicao())
                    .orElse(login);
            logAuditoriaRepository.save(LogAuditoria.builder()
                    .usuarioLogin(login)
                    .usuarioNomeExibicao(nomeExibicao)
                    .acao(acao)
                    .descricao(descricao)
                    .build());
        } catch (Exception e) {
            log.warn("Falha ao gravar auditoria {} de {}: {}", acao, login, e.getMessage());
        }
    }

    public List<LogAuditoria> listarRecentes(int limite) {
        return logAuditoriaRepository.findAllByOrderByDataHoraDesc(PageRequest.of(0, limite));
    }
}
