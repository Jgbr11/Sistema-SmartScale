package br.com.milscale.milscale.adapters.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AutenticacaoIntegrationTest {

    @Autowired private TestRestTemplate http;

    private ResponseEntity<String> login(String cpf, String senha) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", cpf);
        form.add("password", senha);
        return http.postForEntity("/api/auth/login", new HttpEntity<>(form, cabecalhos), String.class);
    }

    @Test
    void trocarSenhaSemSessao_volta401() {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> r = http.postForEntity("/api/auth/senha",
                new HttpEntity<>("{\"senhaAtual\":\"abcdef\",\"senhaNova\":\"ghijkl\"}", cabecalhos), String.class);
        assertThat(r.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void cookieDeSessao_eHttpOnlyESameSiteStrict() {
        ResponseEntity<String> r = login("00000000001", "milscale123");
        assertThat(r.getStatusCode().value()).isEqualTo(200);
        assertThat(r.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .contains("JSESSIONID").contains("HttpOnly").contains("SameSite=Strict");
    }

    @Test
    void logout_volta204SemRedirecionar() {
        String cookie = login("00000000001", "milscale123").getHeaders().getFirst(HttpHeaders.SET_COOKIE).split(";")[0];
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.add(HttpHeaders.COOKIE, cookie);
        ResponseEntity<String> r = http.postForEntity("/api/auth/logout", new HttpEntity<>(cabecalhos), String.class);
        assertThat(r.getStatusCode().value()).isEqualTo(204);
    }
}
