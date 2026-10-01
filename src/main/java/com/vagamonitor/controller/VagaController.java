package com.vagamonitor.controller;

import com.vagamonitor.dto.BuscaDTO;
import com.vagamonitor.dto.RespostaDTO;
import com.vagamonitor.dto.VagaDTO;
import com.vagamonitor.integration.FonteIndisponivelException;
import com.vagamonitor.service.TelegramService;
import com.vagamonitor.service.VagaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vagas")
public class VagaController {

    private static final Logger log = LoggerFactory.getLogger(VagaController.class);

    private final VagaService vagaService;
    private final TelegramService telegramService;

    public VagaController(VagaService vagaService, TelegramService telegramService) {
        this.vagaService = vagaService;
        this.telegramService = telegramService;
    }

    /**
     * POST /api/vagas/buscar
     * Busca vagas sob demanda no portal Gupy e persiste resultados.
     */
    @PostMapping("/buscar")
    public ResponseEntity<?> buscar(@Valid @RequestBody BuscaDTO busca) {
        try {
            List<VagaDTO> vagas = vagaService.buscarEPersistir(
                    busca.getPalavraChave(), busca.getLocalizacao(), busca.getTipo(), busca.getModelo());
            if (vagas.isEmpty()) {
                return ResponseEntity.ok(new RespostaListagem(vagas,
                        "Nenhuma vaga encontrada para estes critérios."));
            }
            return ResponseEntity.ok(new RespostaListagem(vagas, null));
        } catch (FonteIndisponivelException e) {
            log.error("[VagaController] Fonte indisponivel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new RespostaDTO(false,
                            "Fonte de vagas indisponível no momento. Tente novamente em instantes."));
        }
    }

    /**
     * GET /api/vagas/{id}
     * Retorna detalhes de uma vaga pelo ID interno.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obterVaga(@PathVariable Long id) {
        return vagaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/vagas/{id}/telegram
     * Envia a vaga selecionada para o Telegram.
     * Aceita configuracoes personalizadas enviadas pelo usuario na interface
     * ou utiliza as configuracoes padrao do servidor caso nao fornecidas.
     * Protegido por rate limit (veja RateLimitInterceptor).
     */
    @PostMapping("/{id}/telegram")
    public ResponseEntity<RespostaDTO> enviarParaTelegram(
            @PathVariable Long id,
            @RequestBody(required = false) TelegramEnvioDTO telegramConfig) {
        String token = telegramConfig != null ? telegramConfig.getBotToken() : null;
        String chatId = telegramConfig != null ? telegramConfig.getChatId() : null;

        return vagaService.buscarPorId(id)
                .map(vaga -> {
                    RespostaDTO resposta = telegramService.enviarVaga(
                            vaga.getTitulo(), vaga.getEmpresa(), vaga.getUrlOriginal(), token, chatId);
                    HttpStatus status = resposta.isSucesso()
                            ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
                    return ResponseEntity.status(status).body(resposta);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new RespostaDTO(false, "Vaga não encontrada.")));
    }

    // --- DTO para recebimento de credenciais personalizadas do Telegram ---
    public static class TelegramEnvioDTO {
        private String botToken;
        private String chatId;

        public TelegramEnvioDTO() {}

        public TelegramEnvioDTO(String botToken, String chatId) {
            this.botToken = botToken;
            this.chatId = chatId;
        }

        public String getBotToken() { return botToken; }
        public void setBotToken(String botToken) { this.botToken = botToken; }

        public String getChatId() { return chatId; }
        public void setChatId(String chatId) { this.chatId = chatId; }
    }

    // --- Inner class para resposta de listagem ---
    public static class RespostaListagem {
        private List<VagaDTO> vagas;
        private String aviso;

        public RespostaListagem(List<VagaDTO> vagas, String aviso) {
            this.vagas = vagas;
            this.aviso = aviso;
        }
        public List<VagaDTO> getVagas() { return vagas; }
        public String getAviso() { return aviso; }
    }
}
