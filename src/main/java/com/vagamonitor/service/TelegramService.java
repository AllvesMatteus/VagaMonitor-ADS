package com.vagamonitor.service;

import com.vagamonitor.dto.RespostaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

        String texto = String.format(
                "🔍 *Nova Vaga — VagaMonitor*\n\n" +
                "*Título:* %s\n" +
                "*Empresa:* %s\n" +
                "*Link:* %s",
                escaparMarkdown(titulo),
                escaparMarkdown(empresa != null ? empresa : "Não informado"),
                urlOriginal
        );

        String url = telegramBaseUrl + "/bot" + tokenUsado + "/sendMessage";

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatIdUsado);
        body.put("text", texto);
        body.put("parse_mode", "Markdown");

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
        } catch (RestClientException e) {
            log.error("[Telegram] Erro ao enviar mensagem: {}", e.getMessage());
            return new RespostaDTO(false, "Falha ao enviar para o Telegram: " + e.getMessage());
        }
    }

    /** Escapa caracteres especiais do Markdown do Telegram. */
    private String escaparMarkdown(String text) {
        if (text == null) return "";
        return text.replace("_", "\\_").replace("*", "\\*")
                   .replace("[", "\\[").replace("`", "\\`");
    }
}
