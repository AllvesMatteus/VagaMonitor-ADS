package com.vagamonitor.dto;

public class VagaDTO {
    private Long id;
    private String titulo;
    private String empresa;
    private String localizacao;
    private String urlOriginal;
    private String descricao;
    private String fonte;

    public VagaDTO() {}

    public VagaDTO(Long id, String titulo, String empresa, String localizacao,
                   String urlOriginal, String descricao, String fonte) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.localizacao = localizacao;
        this.urlOriginal = urlOriginal;
        this.descricao = descricao;
        this.fonte = fonte;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public String getUrlOriginal() { return urlOriginal; }
    public void setUrlOriginal(String urlOriginal) { this.urlOriginal = urlOriginal; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getFonte() { return fonte; }
    public void setFonte(String fonte) { this.fonte = fonte; }
}
