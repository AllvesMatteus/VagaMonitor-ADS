package com.vagamonitor.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.vagamonitor.dto.VagaDTO;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("ArbeitNowClient - Testes com WireMock")
class ArbeitNowClientTest {

    static WireMockServer wireMock;
    ArbeitNowClient client;

    static final String RESPONSE_BODY = """
            {
              "data": [
                {
                  "slug": "java-dev-001",
                  "title": "Java Developer",
                  "company_name": "Tech Corp",
                  "location": "Berlin, Germany",
                  "remote": false,
                  "url": "https://www.arbeitnow.com/jobs/java-dev-001",
                  "description": "<p>Exciting Java role</p>"
                },
                {
                  "slug": "remote-dev-002",
                  "title": "Remote Engineer",
                  "company_name": "Remote Co",
                  "location": "",
                  "remote": true,
                  "url": "https://www.arbeitnow.com/jobs/remote-dev-002",
                  "description": null
                }
              ]
            }
            """;

    @BeforeAll
    static void setupServer() {
        wireMock = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());
    }

    @AfterAll
    static void teardown() { wireMock.stop(); }

    @BeforeEach
    void setup() {
        client = new ArbeitNowClient(new RestTemplate());
        ReflectionTestUtils.setField(client, "baseUrl", "http://localhost:" + wireMock.port());
    }

    @Test
    @DisplayName("Deve retornar vagas mapeadas corretamente")
    void deveRetornarVagas() {
        stubFor(get(urlPathEqualTo("/api/job-board-api"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_BODY)));

        List<VagaDTO> vagas = client.buscar("java", null);

        assertThat(vagas).hasSize(2);
        assertThat(vagas.get(0).getTitulo()).isEqualTo("Java Developer");
        assertThat(vagas.get(0).getEmpresa()).isEqualTo("Tech Corp");
        assertThat(vagas.get(1).getLocalizacao()).isEqualTo("Remoto");
        assertThat(vagas.get(0).getFonte()).isEqualTo("Arbeitnow");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando API retorna data vazia")
    void deveRetornarListaVaziaQuandoSemResultados() {
        stubFor(get(urlPathEqualTo("/api/job-board-api"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\":[]}")));

        List<VagaDTO> vagas = client.buscar("naoexiste", null);
        assertThat(vagas).isEmpty();
    }

    @Test
    @DisplayName("Deve lancar FonteIndisponivelException quando API retorna 503")
    void deveLancarExcecaoQuandoFonteIndisponivel() {
        stubFor(get(urlPathEqualTo("/api/job-board-api"))
                .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.buscar("java", null))
                .isInstanceOf(ArbeitNowClient.FonteIndisponivelException.class);
    }

    @Test
    @DisplayName("Deve filtrar por localizacao quando informada")
    void deveFiltrarPorLocalizacao() {
        stubFor(get(urlPathEqualTo("/api/job-board-api"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_BODY)));

        List<VagaDTO> vagas = client.buscar("java", "Berlin");
        assertThat(vagas).hasSize(1);
        assertThat(vagas.get(0).getTitulo()).isEqualTo("Java Developer");
    }
}
