package com.vagamonitor.integration;

/**
 * Exceção lançada quando a fonte externa de vagas estiver inacessível ou retornar erro.
 */
public class FonteIndisponivelException extends RuntimeException {

    public FonteIndisponivelException(String message) {
        super(message);
    }

    public FonteIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
