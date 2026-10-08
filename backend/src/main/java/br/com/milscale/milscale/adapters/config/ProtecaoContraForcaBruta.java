package br.com.milscale.milscale.adapters.config;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProtecaoContraForcaBruta {

    static final int MAXIMO_DE_FALHAS = 5;
    static final Duration TEMPO_DE_BLOQUEIO = Duration.ofMinutes(15);
    private static final int LIMITE_DE_REGISTROS = 10_000;

    private record Falhas(int quantidade, Instant ultima) {}

    private final Map<String, Falhas> falhasPorCpf = new ConcurrentHashMap<>();
    private final Clock relogio;

    public ProtecaoContraForcaBruta(Clock relogio) {
        this.relogio = relogio;
    }

    public boolean bloqueado(String cpf) {
        Falhas f = falhasPorCpf.get(cpf);
        if (f == null || f.quantidade() < MAXIMO_DE_FALHAS) return false;
        if (expirou(f)) {
            falhasPorCpf.remove(cpf);
            return false;
        }
        return true;
    }

    public void registrarFalha(String cpf) {
        if (falhasPorCpf.size() > LIMITE_DE_REGISTROS) falhasPorCpf.values().removeIf(this::expirou);
        Instant agora = relogio.instant();
        falhasPorCpf.merge(cpf, new Falhas(1, agora), (atual, nova) -> new Falhas(atual.quantidade() + 1, agora));
    }

    public void limpar(String cpf) {
        falhasPorCpf.remove(cpf);
    }

    private boolean expirou(Falhas f) {
        return Duration.between(f.ultima(), relogio.instant()).compareTo(TEMPO_DE_BLOQUEIO) >= 0;
    }
}
