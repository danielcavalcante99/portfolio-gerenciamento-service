package com.portfolio.gerenciamento.units.security;

import com.portfolio.gerenciamento.configs.properties.CorsProperties;
import com.portfolio.gerenciamento.configs.properties.SecurityProperties;
import com.portfolio.gerenciamento.configs.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityCorsTest.TestController.class)
@Import({SecurityConfig.class, SecurityCorsTest.PropertiesConfig.class})
@TestPropertySource(properties = {
        "spring.security.user.name=admin",
        "spring.security.user.password=admin123",
        "portfolio.cors.allowed-origins=http://localhost:5173"
})
class SecurityCorsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve permitir preflight CORS para origem configurada")
    void devePermitirPreflightCorsParaOrigemConfigurada() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Authorization")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Content-Type")));
    }

    @Test
    @DisplayName("Deve rejeitar preflight CORS para origem nao configurada")
    void deveRejeitarPreflightCorsParaOrigemNaoConfigurada() throws Exception {
        mockMvc.perform(options("/api/test")
                        .header(HttpHeaders.ORIGIN, "http://malicious.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    /**
     * Necessário porque @WebMvcTest carrega contexto parcial onde o
     * @ConfigurationPropertiesScan da aplicação não é executado,
     * e o @EnableConfigurationProperties do SecurityConfig não é suficiente
     * neste contexto de teste slice.
     * <p>
     * Sem este registro manual, o SecurityConfig falha ao inicializar pois
     * SecurityProperties e CorsProperties não são encontradas como beans.
     */
    @TestConfiguration
    @EnableConfigurationProperties({SecurityProperties.class, CorsProperties.class})
    static class PropertiesConfig {
    }

    /**
     * Controller fictício exigido pelo @WebMvcTest.
     *
     * @WebMvcTest requer ao menos um controller no contexto para inicializar.
     * Sem um handler mapeado para o path requisitado, o Spring retorna 404
     * antes do filtro CORS processar a requisição, fazendo o teste falhar.
     * <p>
     * O endpoint em si não é testado — serve apenas como destino válido
     * para as requisições OPTIONS do preflight CORS.
     */    @RestController
    static class TestController {

        @GetMapping("/api/test")
        ResponseEntity<Void> test() {
            return ResponseEntity.ok().build();
        }
    }
}
