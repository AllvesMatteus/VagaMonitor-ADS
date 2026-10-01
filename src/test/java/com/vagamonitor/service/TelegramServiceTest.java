package com.vagamonitor.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.vagamonitor.dto.RespostaDTO;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TelegramService - Testes com WireMock")
class TelegramServiceTest {

    static WireMockServer wireMock;
    TelegramService service;

    @BeforeAll
    static void setupServer() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
    }

    @AfterAll
    static void teardown() {
        wireMock.stop();
    }

    @BeforeEach
    void setup() {
        service = new TelegramService(new RestTemplate());
        ReflectionTestUtils.setField(service, "telegramBaseUrl", "http://localhost:" + wireMock.port());
        ReflectionTestUtils.setField(service, "botToken", "fake-token-123");
        ReflectionTestUtils.setField(service, "chatId", "999999999");
    }

    @Test
    @DisplayName("Deve retornar sucesso quando Telegram responde 200")
    void deveEnviarComSucesso() {
        stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"result\":{}}")));

        RespostaDTO resposta = service.enviarVaga(
                "Desenvolvedor Java Senior",
                "Empresa XPTO",
                "https://example.com/vaga/1");

        assertThat(resposta.isSucesso()).isTrue();
        assertThat(resposta.getMensagem()).contains("sucesso");
    }

    @Test
    @DisplayName("Deve retornar falha quando Telegram responde 401")
    void deveRetornarFalhaComTokenInvalido() {
        stubFor(post(urlPathMatching("/bot.*/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody("{\"ok\":false,\"description\":\"Unauthorized\"}")));

        RespostaDTO resposta = service.enviarVaga("Vaga", "Empresa", "https://example.com");

        assertThat(resposta.isSucesso()).isFalse();
    }

    @Test
    @DisplayName("Deve informar nao-configurado quando token esta vazio")
    void deveInformarNaoConfigurado() {
        ReflectionTestUtils.setField(service, "botToken", "");

        RespostaDTO resposta = service.enviarVaga("Vaga", "Empresa", "https://example.com");

        assertThat(resposta.isSucesso()).isFalse();
        assertThat(resposta.getMensagem()).containsIgnoringCase("não configurado");
    }

    @Test
    @DisplayName("Deve priorizar credenciais personalizadas enviadas pelo usuario")
    void deveEnviarComCredenciaisPersonalizadas() {
        // Servidor com credenciais invalidas/vazias
        ReflectionTestUtils.setField(service, "botToken", "");
        ReflectionTestUtils.setField(service, "chatId", "");

        stubFor(post(urlPathMatching("/botcustom-token/sendMessage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"result\":{}}")));

        RespostaDTO resposta = service.enviarVaga(
                "Desenvolvedor Frontend",
                "Mackenzie Tech",
                "https://example.com/vaga/2",
                "custom-token",
                "12345678");

        assertThat(resposta.isSucesso()).isTrue();
        assertThat(resposta.getMensagem()).contains("sucesso");
    }
}
