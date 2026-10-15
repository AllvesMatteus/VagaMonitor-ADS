package com.vagamonitor.dto;

import java.util.List;

public class ResultadoBuscaDTO {
    private List<VagaDTO> vagas;
    private int total;
    private int pagina;
    private int totalPaginas;
    private int limite;

    public ResultadoBuscaDTO() {}

    public ResultadoBuscaDTO(List<VagaDTO> vagas, int total, int pagina, int totalPaginas, int limite) {
        this.vagas = vagas;
        this.total = total;
        this.pagina = pagina;
        this.totalPaginas = totalPaginas;
        this.limite = limite;
    }

    public List<VagaDTO> getVagas() { return vagas; }
    public void setVagas(List<VagaDTO> vagas) { this.vagas = vagas; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPagina() { return pagina; }
    public void setPagina(int pagina) { this.pagina = pagina; }

    public int getTotalPaginas() { return totalPaginas; }
    public void setTotalPaginas(int totalPaginas) { this.totalPaginas = totalPaginas; }

    public int getLimite() { return limite; }
    public void setLimite(int limite) { this.limite = limite; }
}
