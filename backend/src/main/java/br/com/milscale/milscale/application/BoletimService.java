package br.com.milscale.milscale.application;

import br.com.milscale.milscale.adapters.persistence.BoletimRepository;
import br.com.milscale.milscale.domain.Boletim;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/** Boletim Interno - aberto a leitura pra todo mundo, manutencao privativa de Cabo/Sargenteante. */
@Service
public class BoletimService {

    private final BoletimRepository boletimRepository;
    private final UsuarioLogadoService usuarioLogadoService;

    public BoletimService(BoletimRepository boletimRepository, UsuarioLogadoService usuarioLogadoService) {
        this.boletimRepository = boletimRepository;
        this.usuarioLogadoService = usuarioLogadoService;
    }

    public List<Boletim> listar() {
        return boletimRepository.findAllByOrderByDataPublicacaoDesc();
    }

    public Boletim buscar(Long id) {
        return boletimRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Boletim não encontrado"));
    }

    @Transactional
    public Boletim criar(String numero, String titulo, String conteudoHtml, String avisoRelacionado,
                          String avisoRelacionadoDescricao, String loginAutor) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Informe um título");
        }
        if (conteudoHtml == null || conteudoHtml.isBlank()) {
            throw new IllegalArgumentException("O boletim não pode ficar vazio");
        }
        var autor = usuarioLogadoService.militar(loginAutor);
        return boletimRepository.save(Boletim.builder()
                .numero(numero).titulo(titulo).conteudoHtml(conteudoHtml).autor(autor)
                .avisoRelacionado(avisoRelacionado).avisoRelacionadoDescricao(avisoRelacionadoDescricao)
                .build());
    }

    @Transactional
    public Boletim atualizar(Long id, String numero, String titulo, String conteudoHtml,
                              String avisoRelacionado, String avisoRelacionadoDescricao) {
        Boletim existente = buscar(id);
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Informe um título");
        }
        if (conteudoHtml == null || conteudoHtml.isBlank()) {
            throw new IllegalArgumentException("O boletim não pode ficar vazio");
        }
        existente.setNumero(numero);
        existente.setTitulo(titulo);
        existente.setConteudoHtml(conteudoHtml);
        existente.setAvisoRelacionado(avisoRelacionado);
        existente.setAvisoRelacionadoDescricao(avisoRelacionadoDescricao);
        existente.setDataAtualizacao(LocalDateTime.now());
        return boletimRepository.save(existente);
    }

    @Transactional
    public void remover(Long id) {
        boletimRepository.deleteById(id);
    }
}
