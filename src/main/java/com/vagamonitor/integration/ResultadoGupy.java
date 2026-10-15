package com.vagamonitor.integration;

import com.vagamonitor.dto.VagaDTO;
import java.util.List;

public class ResultadoGupy {
    private final List<VagaDTO> vagas;
    private final int total;
    private final int limit;
    private final int offset;

    public ResultadoGupy(List<VagaDTO> vagas, int total, int limit, int offset) {
        this.vagas = vagas;
        this.total = total;
        this.limit = limit;
        this.offset = offset;
    }

    public List<VagaDTO> getVagas() { return vagas; }
    public int getTotal() { return total; }
    public int getLimit() { return limit; }
    public int getOffset() { return offset; }
}
