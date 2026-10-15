package com.vagamonitor.service;

import com.vagamonitor.dto.RespostaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Servico responsavel por enviar mensagens via Telegram Bot API.
 * Variaveis de ambiente necessarias: TELEGRAM_BOT_TOKEN e TELEGRAM_CHAT_ID
 */
@Service
public class TelegramService {

    private static final Logger log = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.base-url:https://api.telegram.org}")
    private String telegramBaseUrl;

    @Value("${telegram.bot-token:}")
    private String botToken;

    @Value("${telegram.chat-id:}")
    private String chatId;

    private final RestTemplate restTemplate;

    public TelegramService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Retorna true se o bot estiver configurado (token e chat-id presentes no servidor).
     */
    public boolean isConfigurado() {
        return botToken != null && !botToken.isBlank()
                && chatId != null && !chatId.isBlank();
    }

    /**
     * Envia mensagem formatada sobre a vaga usando as credenciais do servidor.
     */
    public RespostaDTO enviarVaga(String titulo, String empresa, String urlOriginal) {
        return enviarVaga(titulo, empresa, urlOriginal, null, null);
    }

    /**
     * Envia mensagem formatada sobre a vaga para o chat do Telegram.
     * Suporta credenciais enviadas diretamente pelo usuário na interface
     * com fallback para as variáveis de ambiente do servidor.
     */
    public RespostaDTO enviarVaga(String titulo, String empresa, String urlOriginal, String customToken, String customChatId) {
        String tokenUsado = (customToken != null && !customToken.isBlank()) ? customToken.trim() : this.botToken;
        String chatIdUsado = (customChatId != null && !customChatId.isBlank()) ? customChatId.trim() : this.chatId;

        if (tokenUsado == null || tokenUsado.isBlank() || chatIdUsado == null || chatIdUsado.isBlank()) {
            return new RespostaDTO(false,
                    "Telegram não configurado. Por favor, preencha o Token do Bot e o Chat ID nas configurações da aplicação.");
        }

        String tituloLimpo = (titulo != null && !titulo.isBlank()) ? titulo.trim() : "Sem título";
        String empresaLimpa = (empresa != null && !empresa.isBlank()) ? empresa.trim() : "Não informada";
        String urlSegura = (urlOriginal != null) ? urlOriginal.trim() : "";

        // Formatação em HTML (robusta contra underscores, asteriscos e caracteres especiais em URLs)
        String textoHtml;
        if (!urlSegura.isBlank()) {
            textoHtml = String.format(
                    "🔍 <b>Nova Vaga — VagaMonitor</b>\n\n" +
                    "💼 <b>Título:</b> %s\n" +
                    "🏢 <b>Empresa:</b> %s\n\n" +
                    "🔗 <a href=\"%s\"><b>Acessar Vaga no Portal Gupy</b></a>\n\n" +
                    "<i>Enviado via VagaMonitor • Mackenzie</i>",
                    escaparHtml(tituloLimpo),
                    escaparHtml(empresaLimpa),
                    escaparHtml(urlSegura)
            );
        } else {
            textoHtml = String.format(
                    "🔍 <b>Nova Vaga — VagaMonitor</b>\n\n" +
                    "💼 <b>Título:</b> %s\n" +
                    "🏢 <b>Empresa:</b> %s\n\n" +
                    "<i>Enviado via VagaMonitor • Mackenzie</i>",
                    escaparHtml(tituloLimpo),
                    escaparHtml(empresaLimpa)
            );
        }

        String url = telegramBaseUrl + "/bot" + tokenUsado + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatIdUsado);
        body.put("text", textoHtml);
        body.put("parse_mode", "HTML");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("[Telegram] Mensagem enviada com sucesso para chat {}", chatIdUsado);
                return new RespostaDTO(true, "Vaga enviada ao Telegram com sucesso!");
            } else {
                log.warn("[Telegram] Resposta nao 2xx: {}", response.getStatusCode());
                return new RespostaDTO(false, "Telegram retornou status " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException e) {
            log.warn("[Telegram] Falha no envio formatado em HTML (status {}): {}. Tentando fallback em texto puro...",
                    e.getStatusCode(), e.getResponseBodyAsString());

            // Fallback resiliente: envia texto puro sem parse_mode caso o parser do Telegram rejeite entidades
            if (e.getStatusCode().is4xxClientError()) {
                try {
                    String textoPuro = String.format(
                            "🔍 Nova Vaga — VagaMonitor\n\n" +
                            "💼 Título: %s\n" +
                            "🏢 Empresa: %s\n" +
                            (urlSegura.isBlank() ? "" : "🔗 Link: " + urlSegura + "\n\n") +
                            "Enviado via VagaMonitor • Mackenzie",
                            tituloLimpo,
                            empresaLimpa
                    );

                    Map<String, Object> fallbackBody = new HashMap<>();
                    fallbackBody.put("chat_id", chatIdUsado);
                    fallbackBody.put("text", textoPuro);

                    HttpEntity<Map<String, Object>> fallbackRequest = new HttpEntity<>(fallbackBody, headers);
                    ResponseEntity<String> fallbackResp = restTemplate.postForEntity(url, fallbackRequest, String.class);
                    if (fallbackResp.getStatusCode().is2xxSuccessful()) {
                        log.info("[Telegram] Mensagem enviada com sucesso via fallback em texto puro para chat {}", chatIdUsado);
                        return new RespostaDTO(true, "Vaga enviada ao Telegram com sucesso!");
                    }
                } catch (RestClientException ex) {
                    log.error("[Telegram] Erro no fallback de texto puro: {}", ex.getMessage());
                }
            }

            return new RespostaDTO(false, "Falha ao enviar para o Telegram: " + extrairMensagemErro(e));
        } catch (RestClientException e) {
            log.error("[Telegram] Erro ao enviar mensagem: {}", e.getMessage());
            return new RespostaDTO(false, "Falha ao enviar para o Telegram: " + e.getMessage());
        }
    }

    /** Escapa caracteres especiais para HTML no Telegram. */
    private String escaparHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }

    /** Extrai mensagem amigavel do corpo de erro da API do Telegram. */
    private String extrairMensagemErro(HttpStatusCodeException e) {
        String responseBody = e.getResponseBodyAsString();
        if (responseBody != null && responseBody.contains("\"description\":\"")) {
            int start = responseBody.indexOf("\"description\":\"") + 15;
            int end = responseBody.indexOf("\"", start);
            if (end > start) {
                return responseBody.substring(start, end);
            }
        }
        return e.getMessage();
    }
}
