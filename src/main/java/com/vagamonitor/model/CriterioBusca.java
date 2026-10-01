package com.vagamonitor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "criterios_busca")
public class CriterioBusca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String palavraChave;

    @Column(length = 256)
    private String localizacao;

    @Column(name = "total_resultados")
    private Integer totalResultados;

    @Column(name = "buscado_em", nullable = false, updatable = false)
    private LocalDateTime buscadoEm = LocalDateTime.now();

    // --- getters/setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPalavraChave() { return palavraChave; }
    public void setPalavraChave(String palavraChave) { this.palavraChave = palavraChave; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }

    public Integer getTotalResultados() { return totalResultados; }
    public void setTotalResultados(Integer totalResultados) { this.totalResultados = totalResultados; }

    public LocalDateTime getBuscadoEm() { return buscadoEm; }
    public void setBuscadoEm(LocalDateTime buscadoEm) { this.buscadoEm = buscadoEm; }
}
