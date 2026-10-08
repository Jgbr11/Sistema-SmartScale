package br.com.milscale.milscale.adapters.web;

import org.springframework.security.core.Authentication;

import java.util.Set;

public final class PerfisSargenteacao {

    private static final Set<String> ROLES = Set.of(
            "ROLE_CABO_SARGENTEACAO", "ROLE_SD_EP_SARGENTEACAO", "ROLE_SARGENTEANTE");

    private PerfisSargenteacao() {}

    public static boolean ehSargenteacao(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> ROLES.contains(a.getAuthority()));
    }
}
