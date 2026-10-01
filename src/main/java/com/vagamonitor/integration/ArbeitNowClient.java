package com.vagamonitor.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vagamonitor.dto.VagaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Integração com a Arbeitnow Job Board API.
 * Documentação: https://www.arbeitnow.com/api/job-board-api
 * Acesso público, sem necessidade de chave de API.
 */
@Component
public class ArbeitNowClient {

    private static final Logger log = LoggerFactory.getLogger(ArbeitNowClient.class);
    private static final String FONTE = "Arbeitnow";

    @Value("${arbeitnow.base-url:https://www.arbeitnow.com}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public ArbeitNowClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Busca vagas na Arbeitnow API filtrando por tag (palavra-chave).
     * A API nao suporta localizacao como parametro; aplicamos filtro local quando fornecido.
     */
    public List<VagaDTO> buscar(String palavraChave, String localizacao) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/job-board-api")
                .queryParam("tag", palavraChave)
                .build()
                .toUriString();

        log.info("[ArbeitNow] Buscando: url={}", url);

        try {
            ResponseEntity<ArbeitNowResponse> response =
                    restTemplate.getForEntity(url, ArbeitNowResponse.class);

            if (response.getBody() == null || response.getBody().getData() == null) {
                log.warn("[ArbeitNow] Resposta vazia ou nula.");
                return Collections.emptyList();
            }

            List<VagaDTO> vagas = response.getBody().getData().stream()
                    .filter(j -> filtrarLocalizacao(j, localizacao))
                    .map(this::toDto)
                    .collect(Collectors.toList());

            log.info("[ArbeitNow] {} vagas retornadas.", vagas.size());
            return vagas;

        } catch (RestClientException e) {
            log.error("[ArbeitNow] Falha ao consultar fonte externa: {}", e.getMessage());
            throw new FonteIndisponivelException("Arbeitnow API indisponivel: " + e.getMessage(), e);
        }
    }

    private boolean filtrarLocalizacao(JobItem job, String localizacao) {
        if (localizacao == null || localizacao.isBlank()) return true;
        String loc = localizacao.trim().toLowerCase();
        String jobLoc = job.getLocation() != null ? job.getLocation().toLowerCase() : "";
        return jobLoc.contains(loc) || (job.isRemote() && loc.contains("remot"));
    }

    private VagaDTO toDto(JobItem job) {
        String loc = job.isRemote() ? "Remoto" : (job.getLocation() != null ? job.getLocation() : "N/I");
        String descricao = job.getDescription() != null
                ? job.getDescription().replaceAll("<[^>]+>", "").trim()
                : null;
        if (descricao != null && descricao.length() > 1000) {
            descricao = descricao.substring(0, 997) + "...";
        }
        return new VagaDTO(null, job.getTitle(), job.getCompanyName(), loc,
                job.getUrl(), descricao, FONTE);
    }

    // ---- Inner classes for deserialization ----

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ArbeitNowResponse {
        private List<JobItem> data;
        public List<JobItem> getData() { return data; }
        public void setData(List<JobItem> data) { this.data = data; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JobItem {
        private String slug;
        private String title;
        @JsonProperty("company_name")
        private String companyName;
        private String location;
        private boolean remote;
        private String url;
        private String description;

        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public boolean isRemote() { return remote; }
        public void setRemote(boolean remote) { this.remote = remote; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /** Excecao indicando que a fonte externa esta indisponivel. */
    public static class FonteIndisponivelException extends com.vagamonitor.integration.FonteIndisponivelException {
        public FonteIndisponivelException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
