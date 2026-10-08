package br.com.milscale.milscale.adapters.config;

import br.com.milscale.milscale.adapters.persistence.*;
import br.com.milscale.milscale.domain.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(1)
public class DadosDeReferenciaSeeder implements CommandLineRunner {

    private final SubunidadeRepository subunidadeRepository;
    private final PostoGraduacaoRepository postoRepository;
    private final QualificacaoRepository qualificacaoRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final RequisitoServicoRepository requisitoServicoRepository;
    private final RegraEscalaRepository regraEscalaRepository;
    private final PerfilAcessoRepository perfilAcessoRepository;

    public DadosDeReferenciaSeeder(SubunidadeRepository subunidadeRepository, PostoGraduacaoRepository postoRepository,
                                   QualificacaoRepository qualificacaoRepository, TipoServicoRepository tipoServicoRepository,
                                   RequisitoServicoRepository requisitoServicoRepository, RegraEscalaRepository regraEscalaRepository,
                                   PerfilAcessoRepository perfilAcessoRepository) {
        this.subunidadeRepository = subunidadeRepository;
        this.postoRepository = postoRepository;
        this.qualificacaoRepository = qualificacaoRepository;
        this.tipoServicoRepository = tipoServicoRepository;
        this.requisitoServicoRepository = requisitoServicoRepository;
        this.regraEscalaRepository = regraEscalaRepository;
        this.perfilAcessoRepository = perfilAcessoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (subunidadeRepository.count() > 0) return;

        // ---- Organização ----
        subunidadeRepository.save(Subunidade.builder().sigla("CCAp").nome("Companhia de Comando e Apoio").build());
        subunidadeRepository.save(Subunidade.builder().sigla("1 Cia").nome("Primeira Companhia").build());
        Subunidade aprov = subunidadeRepository.save(Subunidade.builder().sigla("Aprov").nome("Aprovisionamento").build());

        PostoGraduacao sdEv = postoRepository.save(PostoGraduacao.builder().sigla("Sd EV").descricao("Soldado EV").nivelHierarquico(1).build());
        PostoGraduacao sdEp = postoRepository.save(PostoGraduacao.builder().sigla("Sd EP").descricao("Soldado EP").nivelHierarquico(2).build());
        PostoGraduacao cb = postoRepository.save(PostoGraduacao.builder().sigla("Cb").descricao("Cabo").nivelHierarquico(3).build());
        PostoGraduacao sgt3 = postoRepository.save(PostoGraduacao.builder().sigla("3 Sgt").descricao("Terceiro-Sargento").nivelHierarquico(4).build());
        PostoGraduacao sgt2 = postoRepository.save(PostoGraduacao.builder().sigla("2 Sgt").descricao("Segundo-Sargento").nivelHierarquico(5).build());
        PostoGraduacao ten = postoRepository.save(PostoGraduacao.builder().sigla("Ten").descricao("Tenente").nivelHierarquico(6).build());

        Qualificacao cfc = qualificacaoRepository.save(Qualificacao.builder().nome("CFC").descricao("Curso de Formação de Cabos — preparação para Cabo da Guarda").build());
        Qualificacao motorista = qualificacaoRepository.save(Qualificacao.builder().nome("Motorista").descricao("Habilitação de motorista militar").build());

        // ---- Tipos de serviço e regras ----
        record TipoDef(String nome, int efetivo) {}
        List<TipoDef> defs = List.of(
                new TipoDef("Oficial de Dia", 1),
                new TipoDef("Graduado de Dia", 1),
                new TipoDef("Comandante da Guarda", 1),
                new TipoDef("Cabo da Guarda", 1),
                new TipoDef("Cabo de Dia", 1),
                new TipoDef("Motorista de Dia", 1),
                new TipoDef("Monitoramento", 3),
                new TipoDef("Plantão ao Alojamento", 3),
                new TipoDef("Graduado do Rancho", 1),
                new TipoDef("Cozinheiro de Dia", 1),
                new TipoDef("Rancheiro de Dia", 2),
                new TipoDef("Guardas ao Quartel", 9)
        );
        Map<String, TipoServico> tipos = new LinkedHashMap<>();
        for (TipoDef d : defs) {
            TipoServico t = tipoServicoRepository.save(TipoServico.builder().nome(d.nome()).efetivoNecessario(d.efetivo()).build());
            tipos.put(d.nome(), t);
            regraEscalaRepository.save(RegraEscala.builder().tipoServico(t).intervaloMinimo(3).diasFolga(1).maxServicosMes(10).build());
        }

        // ---- Elegibilidade ----
        salvarRequisito(tipos.get("Oficial de Dia"), ten, null, null);
        salvarRequisitoExcluindo(tipos.get("Graduado de Dia"), sgt3, aprov);
        salvarRequisitoExcluindo(tipos.get("Graduado de Dia"), sgt2, aprov);
        salvarRequisitoExcluindo(tipos.get("Comandante da Guarda"), sgt3, aprov);
        salvarRequisitoExcluindo(tipos.get("Cabo da Guarda"), cb, aprov);
        salvarRequisito(tipos.get("Cabo de Dia"), sdEp, null, cfc);
        salvarRequisito(tipos.get("Motorista de Dia"), sdEp, null, motorista);
        requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipos.get("Monitoramento")).posto(sdEp)
                .qualificacoesExcluidas(new HashSet<>(List.of(cfc, motorista)))
                .subunidadeExcluida(aprov)
                .build());
        salvarRequisitoExcluindo(tipos.get("Plantão ao Alojamento"), sdEv, aprov);
        salvarRequisito(tipos.get("Graduado do Rancho"), sgt3, aprov, null);
        salvarRequisito(tipos.get("Graduado do Rancho"), sgt2, aprov, null);
        salvarRequisito(tipos.get("Cozinheiro de Dia"), sdEp, aprov, null);
        salvarRequisito(tipos.get("Cozinheiro de Dia"), cb, aprov, null);
        salvarRequisito(tipos.get("Rancheiro de Dia"), sdEv, aprov, null);
        salvarRequisitoExcluindo(tipos.get("Guardas ao Quartel"), sdEv, aprov);

        // ---- Perfis de acesso ----
        perfilAcessoRepository.save(PerfilAcesso.builder().nome("MILITAR_ESCALADO")
                .descricao("Consulta a própria escala e abre solicitações de permuta, dispensa e férias").build());
        perfilAcessoRepository.save(PerfilAcesso.builder().nome("SD_EP_SARGENTEACAO")
                .descricao("Consulta a escala completa, as solicitações e o histórico de serviços").build());
        perfilAcessoRepository.save(PerfilAcesso.builder().nome("CABO_SARGENTEACAO")
                .descricao("Mantém cadastros e afastamentos, gera a escala e faz a triagem das solicitações").build());
        perfilAcessoRepository.save(PerfilAcesso.builder().nome("SARGENTEANTE")
                .descricao("Edita a escala, mantém regras e tipos de serviço, trava serviços, publica a escala e autoriza as solicitações").build());
    }

    private void salvarRequisito(TipoServico tipo, PostoGraduacao posto, Subunidade subunidade, Qualificacao qualificacao) {
        requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipo).posto(posto).subunidade(subunidade).qualificacao(qualificacao).build());
    }

    private void salvarRequisitoExcluindo(TipoServico tipo, PostoGraduacao posto, Subunidade subunidadeExcluida) {
        requisitoServicoRepository.save(RequisitoServico.builder()
                .tipoServico(tipo).posto(posto).subunidadeExcluida(subunidadeExcluida).build());
    }
}
