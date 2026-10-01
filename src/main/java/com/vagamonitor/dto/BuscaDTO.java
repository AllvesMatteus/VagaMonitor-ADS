package com.vagamonitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BuscaDTO {

    @NotBlank(message = "Palavra-chave obrigatoria")
    @Size(min = 2, max = 200)
    private String palavraChave;

    @Size(max = 200)
    private String localizacao;

    private String tipo;

    private String modelo;

    public String getPalavraChave() { return palavraChave; }
    public void setPalavraChave(String palavraChave) { this.palavraChave = palavraChave; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
}

