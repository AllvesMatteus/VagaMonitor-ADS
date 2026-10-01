package com.vagamonitor.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vagamonitor.dto.VagaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cliente de integração com o portal público de vagas da Gupy.
 * Endpoint consumido: https://portal.gupy.io/api/job-search/jobs
 * Acesso direto a oportunidades reais em empresas do Brasil.
 */
@Component
public class GupyClient {

    private static final Logger log = LoggerFactory.getLogger(GupyClient.class);
    private static final String FONTE = "Gupy";

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final Map<String, String> TRADUCAO_MODELO = Map.of(
            "remote", "Remoto",
            "hybrid", "Híbrido",
            "on-site", "Presencial"
    );

    private static final Map<String, String> TRADUCAO_TIPO = Map.of(
            "vacancy_type_effective", "Efetivo",
            "vacancy_type_internship", "Estágio",
            "vacancy_type_apprentice", "Jovem Aprendiz",
            "vacancy_type_temporary", "Temporário",
            "vacancy_type_freelancer", "Freelancer",
            "vacancy_type_associate", "Associado",
            "vacancy_type_talent_pool", "Banco de Talentos"
    );

    @Value("${gupy.base-url:https://portal.gupy.io}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public GupyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Busca vagas no portal da Gupy por palavra-chave e filtros opcionais.
     */
    public List<VagaDTO> buscar(String palavraChave, String localizacao, String tipo, String modelo) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/job-search/jobs")
                .queryParam("limit", 20)
                .queryParam("offset", 0);

        if (palavraChave != null && !palavraChave.isBlank()) {
            uriBuilder.queryParam("jobName", palavraChave.trim());
        }

        if (localizacao != null && !localizacao.isBlank()) {
            uriBuilder.queryParam("state", localizacao.trim());
        }

        if (modelo != null && !modelo.isBlank()) {
            uriBuilder.queryParam("workplaceType", modelo.trim());
        }

        if (tipo != null && !tipo.isBlank()) {
            uriBuilder.queryParam("type", tipo.trim());
        }

        String url = uriBuilder.build().toUriString();
        log.info("[GupyClient] Consultando vagas: url={}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        headers.set(HttpHeaders.ACCEPT, "application/json, text/plain, */*");
        headers.set(HttpHeaders.REFERER, baseUrl + "/job-search");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GupyResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    GupyResponse.class
            );

            if (response.getBody() == null || response.getBody().getData() == null) {
                log.warn("[GupyClient] Resposta vazia ou nula recebida.");
                return Collections.emptyList();
            }

            List<VagaDTO> vagas = response.getBody().getData().stream()
                    .filter(item -> item.getJobUrl() != null && !item.getJobUrl().isBlank())
                    .map(this::toDto)
                    .collect(Collectors.toList());

            log.info("[GupyClient] {} vagas retornadas com sucesso.", vagas.size());
            return vagas;

        } catch (RestClientException e) {
            log.error("[GupyClient] Falha na comunicação com a API da Gupy: {}", e.getMessage());
            throw new FonteIndisponivelException("Portal de vagas Gupy indisponível no momento: " + e.getMessage(), e);
        }
    }

    public List<VagaDTO> buscar(String palavraChave, String localizacao) {
        return buscar(palavraChave, localizacao, null, null);
    }

    private VagaDTO toDto(GupyJobItem item) {
        String titulo = (item.getName() != null && !item.getName().isBlank())
                ? item.getName().trim() : "Título não informado";

        String empresa = (item.getCareerPageName() != null && !item.getCareerPageName().isBlank())
                ? item.getCareerPageName().trim() : "Confidencial / Empresa parceira";

        String localizacao = formatarLocalizacao(item);
        String descricao = formatarDescricao(item.getDescription());

        return new VagaDTO(null, titulo, empresa, localizacao, item.getJobUrl(), descricao, FONTE);
    }

    private String formatarLocalizacao(GupyJobItem item) {
        String modeloTraduzido = TRADUCAO_MODELO.getOrDefault(item.getWorkplaceType(), "");
        if ("remote".equalsIgnoreCase(item.getWorkplaceType())) {
            return "Remoto";
        }

        StringBuilder sb = new StringBuilder();
        if (item.getCity() != null && !item.getCity().isBlank()) {
            sb.append(item.getCity().trim());
        }
        if (item.getState() != null && !item.getState().isBlank()) {
            if (!sb.isEmpty()) sb.append(" - ");
            sb.append(item.getState().trim());
        }

        if (!modeloTraduzido.isBlank()) {
            if (!sb.isEmpty()) sb.append(" (").append(modeloTraduzido).append(")");
            else sb.append(modeloTraduzido);
        }

        return !sb.isEmpty() ? sb.toString() : "Não informado";
    }

    private String formatarDescricao(String descHtml) {
        if (descHtml == null || descHtml.isBlank()) return null;
        String limpo = descHtml
                .replaceAll("<[^>]+>", " ")
                .replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&quot;", "\"")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("\\s+", " ")
                .trim();
        if (limpo.length() > 1000) {
            limpo = limpo.substring(0, 997) + "...";
        }
        return limpo;
    }

    // ---- DTOs internos para desserialização do JSON da Gupy ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GupyResponse {
        private List<GupyJobItem> data;
        private GupyPagination pagination;

        public List<GupyJobItem> getData() { return data; }
        public void setData(List<GupyJobItem> data) { this.data = data; }

        public GupyPagination getPagination() { return pagination; }
        public void setPagination(GupyPagination pagination) { this.pagination = pagination; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GupyPagination {
        private Integer total;
        private Integer limit;
        private Integer offset;

        public Integer getTotal() { return total; }
        public void setTotal(Integer total) { this.total = total; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }

        public Integer getOffset() { return offset; }
        public void setOffset(Integer offset) { this.offset = offset; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GupyJobItem {
        private Long id;
        private String name;
        private String description;
        private String careerPageName;
        private String careerPageLogo;
        private String type;
        private String publishedDate;
        private String workplaceType;
        private String city;
        private String state;
        private String jobUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getCareerPageName() { return careerPageName; }
        public void setCareerPageName(String careerPageName) { this.careerPageName = careerPageName; }

        public String getCareerPageLogo() { return careerPageLogo; }
        public void setCareerPageLogo(String careerPageLogo) { this.careerPageLogo = careerPageLogo; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPublishedDate() { return publishedDate; }
        public void setPublishedDate(String publishedDate) { this.publishedDate = publishedDate; }

        public String getWorkplaceType() { return workplaceType; }
        public void setWorkplaceType(String workplaceType) { this.workplaceType = workplaceType; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getJobUrl() { return jobUrl; }
        public void setJobUrl(String jobUrl) { this.jobUrl = jobUrl; }
    }
}
