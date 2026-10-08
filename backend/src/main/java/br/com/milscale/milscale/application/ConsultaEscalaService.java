package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.EscalaRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.Escala;
import br.com.milscale.milscale.domain.ServicoEscalado;
import br.com.milscale.milscale.domain.SituacaoEscala;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ConsultaEscalaService {

    private final EscalaRepository escalaRepository;
    private final ServicoEscaladoRepository servicoEscaladoRepository;

    public ConsultaEscalaService(EscalaRepository escalaRepository, ServicoEscaladoRepository servicoEscaladoRepository) {
        this.escalaRepository = escalaRepository;
        this.servicoEscaladoRepository = servicoEscaladoRepository;
    }

    public List<EscalaResumo> listar() {
        return escalaRepository.findAllByOrderByDataInicioDesc().stream().map(this::resumo).toList();
    }

    public EscalaDoMes doMes(YearMonth mes) {
        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();
        List<EscalaResumo> escalas = escalaRepository
                .findByDataInicioLessThanEqualAndDataFimGreaterThanEqualOrderByDataInicioAsc(fim, inicio)
                .stream().map(this::resumo).toList();
        return new EscalaDoMes(escalas, servicoEscaladoRepository.findByDataBetween(inicio, fim));
    }

    private EscalaResumo resumo(Escala e) {
        return new EscalaResumo(e.getId(), e.getDescricao(), e.getDataInicio(), e.getDataFim(), e.getSituacao(),
                e.getDataPublicacao(), servicoEscaladoRepository.countByEscala_Id(e.getId()),
                servicoEscaladoRepository.countByEscala_IdAndMilitarIsNull(e.getId()));
    }

    public Escala buscar(Long id) {
        return escalaRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Escala não encontrada"));
    }

    public List<ServicoEscalado> doDia(LocalDate data, boolean incluirRascunho) {
        return incluirRascunho
                ? servicoEscaladoRepository.findByData(data)
                : servicoEscaladoRepository.findByDataAndEscala_Situacao(data, SituacaoEscala.PUBLICADA);
    }
}
