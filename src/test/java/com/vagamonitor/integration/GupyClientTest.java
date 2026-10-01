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

@DisplayName("GupyClient - Testes com WireMock")
class GupyClientTest {

    static WireMockServer wireMock;
    GupyClient client;

    static final String RESPONSE_BODY = """
            {
              "data": [
                {
                  "id": 12649702,
                  "name": "Desenvolvedor Java/Python",
                  "description": "<p>Vaga para atuar com desenvolvimento backend moderno.</p>",
                  "careerPageName": "Empresa Tech XP",
                  "careerPageLogo": "",
                  "type": "vacancy_type_effective",
                  "publishedDate": "2026-10-01T00:00:00.000Z",
                  "workplaceType": "hybrid",
                  "city": "São Paulo",
                  "state": "São Paulo",
                  "jobUrl": "https://empresatech.gupy.io/job/12649702"
                },
                {
                  "id": 12649703,
                  "name": "Estágio em TI",
                  "description": "Oportunidade para estudantes.",
                  "careerPageName": "Inovação Brasil",
                  "careerPageLogo": "",
                  "type": "vacancy_type_internship",
                  "publishedDate": "2026-10-01T00:00:00.000Z",
                  "workplaceType": "remote",
                  "city": "",
                  "state": "",
                  "jobUrl": "https://inovacao.gupy.io/job/12649703"
                }
              ],
              "pagination": {
                "total": 2,
                "limit": 20,
                "offset": 0
              }
            }
            """;

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
        client = new GupyClient(new RestTemplate());
        ReflectionTestUtils.setField(client, "baseUrl", "http://localhost:" + wireMock.port());
    }

    @Test
    @DisplayName("Deve mapear vagas da Gupy com sucesso para VagaDTO")
    void deveRetornarVagasMapeadas() {
        stubFor(get(urlPathEqualTo("/api/job-search/jobs"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(RESPONSE_BODY)));

        List<VagaDTO> vagas = client.buscar("Java", "São Paulo", null, "hybrid");

        assertThat(vagas).hasSize(2);

        VagaDTO vaga1 = vagas.get(0);
        assertThat(vaga1.getTitulo()).isEqualTo("Desenvolvedor Java/Python");
        assertThat(vaga1.getEmpresa()).isEqualTo("Empresa Tech XP");
        assertThat(vaga1.getLocalizacao()).contains("São Paulo - São Paulo (Híbrido)");
        assertThat(vaga1.getUrlOriginal()).isEqualTo("https://empresatech.gupy.io/job/12649702");
        assertThat(vaga1.getFonte()).isEqualTo("Gupy");
        assertThat(vaga1.getDescricao()).doesNotContain("<p>").contains("Vaga para atuar");

        VagaDTO vaga2 = vagas.get(1);
        assertThat(vaga2.getTitulo()).isEqualTo("Estágio em TI");
        assertThat(vaga2.getLocalizacao()).isEqualTo("Remoto");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando resposta não contém vagas")
    void deveRetornarListaVaziaQuandoSemResultados() {
        stubFor(get(urlPathEqualTo("/api/job-search/jobs"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\": [], \"pagination\": {\"total\": 0}}")));

        List<VagaDTO> vagas = client.buscar("naoexiste", null);
        assertThat(vagas).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar FonteIndisponivelException quando Gupy retornar erro HTTP")
    void deveLancarExcecaoQuandoErroHttp() {
        stubFor(get(urlPathEqualTo("/api/job-search/jobs"))
                .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> client.buscar("java", null))
                .isInstanceOf(FonteIndisponivelException.class)
                .hasMessageContaining("Portal de vagas Gupy indisponível");
    }
}
