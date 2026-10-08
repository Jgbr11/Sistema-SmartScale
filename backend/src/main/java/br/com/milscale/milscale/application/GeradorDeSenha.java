package br.com.milscale.milscale.application;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GeradorDeSenha {

    private static final String ALFABETO = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 10;
    private final SecureRandom random = new SecureRandom();

    public String gerar() {
        StringBuilder sb = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
