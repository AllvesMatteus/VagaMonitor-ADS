package com.vagamonitor.service;

import com.vagamonitor.dto.VagaDTO;
import com.vagamonitor.integration.GupyClient;
import com.vagamonitor.model.CriterioBusca;
import com.vagamonitor.model.Vaga;
import com.vagamonitor.repository.CriterioBuscaRepository;
import com.vagamonitor.repository.VagaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VagaService {

    private static final Logger log = LoggerFactory.getLogger(VagaService.class);

    private final GupyClient gupyClient;
    private final VagaRepository vagaRepository;
    private final CriterioBuscaRepository criterioRepository;

    public VagaService(GupyClient gupyClient,
                       VagaRepository vagaRepository,
                       CriterioBuscaRepository criterioRepository) {
        this.gupyClient = gupyClient;
        this.vagaRepository = vagaRepository;
        this.criterioRepository = criterioRepository;
    }

    /**
     * Busca vagas sob demanda no portal Gupy, persiste o critério e as vagas novas, e retorna os DTOs.
     */
    @Transactional
    public List<VagaDTO> buscarEPersistir(String palavraChave, String localizacao, String tipo, String modelo) {
        List<VagaDTO> vagasExternas = gupyClient.buscar(palavraChave, localizacao, tipo, modelo);

        // Persiste o critério de busca
        CriterioBusca criterio = new CriterioBusca();
        criterio.setPalavraChave(palavraChave != null ? palavraChave : "");

        StringBuilder locFiltro = new StringBuilder();
        if (localizacao != null && !localizacao.isBlank()) {
            locFiltro.append(localizacao.trim());
        }
        if (modelo != null && !modelo.isBlank()) {
            if (!locFiltro.isEmpty()) locFiltro.append(" | ");
            locFiltro.append("Modelo: ").append(modelo);
        }
        if (tipo != null && !tipo.isBlank()) {
            if (!locFiltro.isEmpty()) locFiltro.append(" | ");
            locFiltro.append("Tipo: ").append(tipo);
        }
        criterio.setLocalizacao(!locFiltro.isEmpty() ? locFiltro.toString() : null);
        criterio.setTotalResultados(vagasExternas.size());
        criterioRepository.save(criterio);

        // Persiste apenas vagas novas (deduplicação por idExterno = URL)
        List<Vaga> vagasSalvas = vagasExternas.stream()
                .map(dto -> persistirSeDuplicada(dto, palavraChave))
                .collect(Collectors.toList());

        return vagasSalvas.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<VagaDTO> buscarEPersistir(String palavraChave, String localizacao) {
        return buscarEPersistir(palavraChave, localizacao, null, null);
    }

    private Vaga persistirSeDuplicada(VagaDTO dto, String palavraChave) {
        String idExterno = dto.getUrlOriginal(); // URL como identificador estável
        Optional<Vaga> existente = vagaRepository.findByIdExterno(idExterno);
        if (existente.isPresent()) {
            log.debug("[VagaService] Vaga já existe no banco, aproveitando registro: {}", idExterno);
            return existente.get();
        }
        Vaga vaga = new Vaga();
        vaga.setTitulo(dto.getTitulo() != null ? dto.getTitulo() : "Sem título");
        vaga.setEmpresa(dto.getEmpresa());
        vaga.setLocalizacao(dto.getLocalizacao());
        vaga.setUrlOriginal(dto.getUrlOriginal());
        vaga.setIdExterno(idExterno);
        vaga.setDescricao(dto.getDescricao());
        vaga.setFonte(dto.getFonte());
        return vagaRepository.save(vaga);
    }

    /**
     * Busca vaga pelo ID interno (banco local).
     */
    public Optional<VagaDTO> buscarPorId(Long id) {
        return vagaRepository.findById(id).map(this::toDTO);
    }

    private VagaDTO toDTO(Vaga v) {
        return new VagaDTO(v.getId(), v.getTitulo(), v.getEmpresa(),
                v.getLocalizacao(), v.getUrlOriginal(), v.getDescricao(), v.getFonte());
    }
}
