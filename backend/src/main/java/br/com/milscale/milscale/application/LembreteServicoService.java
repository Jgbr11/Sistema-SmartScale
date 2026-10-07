package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.IdentidadeDaOrganizacao;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.SituacaoEscala;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class LembreteServicoService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final EnvioDeEmail envioDeEmail;

    public LembreteServicoService(ServicoEscaladoRepository servicoEscaladoRepository, EnvioDeEmail envioDeEmail) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.envioDeEmail = envioDeEmail;
    }

    @Transactional(readOnly = true)
    public int enviarPara(LocalDate dia) {
        int processados = 0;
        for (ServicoEscalado s : servicoEscaladoRepository.findByData(dia)) {
            if (s.getMilitar() == null || s.getEscala().getSituacao() != SituacaoEscala.PUBLICADA) continue;
            String email = s.getMilitar().getEmail();
            if (email == null || email.isBlank()) continue;
            envioDeEmail.enviar(email,
                    "MilScale — você está escalado amanhã (" + dia.format(DATA_BR) + ")",
                    "Olá, " + s.getMilitar().getNomeExibicao() + ".\n\n"
                            + "Você está escalado para \"" + s.getTipoServico().getNome() + "\" no dia " + dia.format(DATA_BR) + ".\n\n"
                            + "Qualquer imprevisto, procure o Cabo ou o Sargenteante com antecedência.\n\n"
                            + "— " + IdentidadeDaOrganizacao.INSTANCIA.assinatura());
            processados++;
        }
        return processados;
    }
}
