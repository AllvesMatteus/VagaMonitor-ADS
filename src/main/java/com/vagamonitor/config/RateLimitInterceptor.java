package com.vagamonitor.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita requisicoes ao endpoint de envio Telegram por IP.
 * Permite no maximo 5 envios por hora por endereco IP.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private static final int LIMITE_POR_HORA = 5;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String ip = obterIp(request);
        Bucket bucket = buckets.computeIfAbsent(ip, this::novoBucket);

        if (bucket.tryConsume(1)) {
            return true;
        }

        log.warn("[RateLimit] IP {} excedeu o limite de envios ao Telegram.", ip);
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"sucesso\":false,\"mensagem\":\"Limite de " + LIMITE_POR_HORA +
                " envios por hora atingido. Tente novamente mais tarde.\"}");
        return false;
    }

    private Bucket novoBucket(String ip) {
        Bandwidth limite = Bandwidth.builder()
                .capacity(LIMITE_POR_HORA)
                .refillGreedy(LIMITE_POR_HORA, Duration.ofHours(1))
                .build();
        return Bucket.builder().addLimit(limite).build();
    }

    private String obterIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
