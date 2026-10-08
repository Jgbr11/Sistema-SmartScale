package br.com.milscale.milscale.application.relatorios;

import br.com.milscale.milscale.adapters.persistence.AfastamentoRepository;
import br.com.milscale.milscale.adapters.persistence.MilitarRepository;
import br.com.milscale.milscale.adapters.persistence.ServicoEscaladoRepository;
import br.com.milscale.milscale.domain.Afastamento;
import br.com.milscale.milscale.domain.Militar;
import br.com.milscale.milscale.domain.ServicoEscalado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final ServicoEscaladoRepository servicoEscaladoRepository;
    private final MilitarRepository militarRepository;
    private final AfastamentoRepository afastamentoRepository;

    public RelatorioService(ServicoEscaladoRepository servicoEscaladoRepository, MilitarRepository militarRepository,
                            AfastamentoRepository afastamentoRepository) {
        this.servicoEscaladoRepository = servicoEscaladoRepository;
        this.militarRepository = militarRepository;
        this.afastamentoRepository = afastamentoRepository;
    }

    public RelatorioArquivo escalaDoDia(LocalDate dia) {
        List<ServicoEscalado> servicos = servicoEscaladoRepository.findByData(dia).stream()
                .sorted(Comparator.comparing((ServicoEscalado s) -> s.getTipoServico().getId()).thenComparing(ServicoEscalado::getId))
                .toList();
        return RelatorioArquivo.de(new RelatorioEscalaDoDia(dia, servicos));
    }

    public RelatorioArquivo servicosDoMilitar(Long militarId) {
        Militar militar = militarRepository.findById(militarId)
                .orElseThrow(() -> new NoSuchElementException("Militar não encontrado"));
        return RelatorioArquivo.de(new RelatorioServicosDoMilitar(militar, servicoEscaladoRepository.findByMilitar_IdOrderByDataDesc(militarId)));
    }

    public RelatorioArquivo afastamentosDoMes(YearMonth mes) {
        List<Afastamento> afastamentos = afastamentoRepository
                .findByDataInicioLessThanEqualAndDataFimGreaterThanEqual(mes.atEndOfMonth(), mes.atDay(1)).stream()
                .sorted(Comparator.comparing(Afastamento::getDataInicio).thenComparing(a -> a.getMilitar().getNomeExibicao()))
                .toList();
        return RelatorioArquivo.de(new RelatorioAfastamentosDoMes(mes, afastamentos));
    }
}
