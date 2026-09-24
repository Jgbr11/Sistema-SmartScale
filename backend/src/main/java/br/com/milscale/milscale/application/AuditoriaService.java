package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.LogAuditoriaRepository;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import br.com.milscale.milscale.domain.LogAuditoria;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Log de auditoria - quem fez o que. Chamado pelos controllers logo
 * depois de uma acao que muda estado dar certo (nunca antes - so
 * registra o que realmente aconteceu). Nunca lanca excecao: uma falha
 * ao gravar o log jamais deve derrubar a acao que estava sendo
 * auditada.
 */
@Service
public class AuditoriaService {

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
            // log de auditoria nunca pode quebrar a acao que estava sendo auditada
        }
    }

    public List<LogAuditoria> listarRecentes(int limite) {
        return logAuditoriaRepository.findAllByOrderByDataHoraDesc(PageRequest.of(0, limite));
    }
}
