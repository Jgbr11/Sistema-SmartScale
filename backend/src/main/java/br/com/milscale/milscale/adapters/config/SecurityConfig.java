package br.com.milscale.milscale.adapters.config;

import jakarta.servlet.http.HttpServletResponse;
import br.com.milscale.milscale.adapters.persistence.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

/**
 * Primeira fatia (RNF02): controle de acesso baseado em perfil,
 * validado no servidor. Sessao simples via cookie (formLogin), sem
 * JWT/refresh-token ainda - "mais raso agora, robustece depois" foi a
 * decisao explicita para esta fatia. CSRF fica desligado porque o
 * front-end e uma SPA separada consumindo a API via JSON, nao forms
 * HTML servidos pelo proprio backend.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final List<String> origensPermitidas;
    private final UsuarioRepository usuarioRepository;
    private final ProtecaoContraForcaBruta protecao;

    public SecurityConfig(@Value("${milscale.cors.origens:http://localhost:*}") List<String> origensPermitidas,
                          UsuarioRepository usuarioRepository, ProtecaoContraForcaBruta protecao) {
        this.origensPermitidas = origensPermitidas;
        this.usuarioRepository = usuarioRepository;
        this.protecao = protecao;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/organizacao").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, a) -> {
                    protecao.limpar(a.getName());
                    res.setStatus(HttpServletResponse.SC_OK);
                })
                .failureHandler((req, res, e) -> {
                    if (e instanceof LockedException) {
                        responderErro(res, 423, "Muitas tentativas erradas. Tente de novo em 15 minutos.");
                        return;
                    }
                    protecao.registrarFalha(somenteDigitos(req.getParameter("username")));
                    responderErro(res, HttpServletResponse.SC_UNAUTHORIZED, "CPF ou senha inválidos");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, a) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        http.addFilterAfter(new SenhaTemporariaFilter(usuarioRepository), AuthorizationFilter.class);
        return http.build();
    }

    private static String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    private static void responderErro(HttpServletResponse res, int status, String mensagem) throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");
        res.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(origensPermitidas);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
