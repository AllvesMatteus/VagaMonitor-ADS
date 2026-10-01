/**
 * VagaMonitor — Frontend JavaScript
 * Padrões de Interação e Design System baseados no Name That UI (namethatui.com)
 */

'use strict';

// === Estado da Aplicação ===
let vagasAtual = [];
let vagaSelecionada = null;
let filtroModeloAtual = '';
let filtroTipoAtual = '';

// === Constantes SVG Reutilizáveis (Substitutos Oficiais de Emojis) ===
const SVGS = {
    telegram: `<svg class="svg-icon svg-icon-tg" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm4.64 6.8c-.15 1.58-.8 5.42-1.13 7.19-.14.75-.42 1-.68 1.03-.58.05-1.02-.38-1.58-.75-.88-.58-1.38-.94-2.23-1.5-.99-.65-.35-1.01.22-1.59.15-.15 2.71-2.48 2.76-2.69a.2.2 0 00-.05-.18c-.06-.05-.14-.03-.21-.02-.09.02-1.49.95-4.22 2.79-.4.27-.76.41-1.08.4-.36-.01-1.04-.2-1.55-.37-.63-.2-1.12-.31-1.08-.66.02-.18.27-.36.74-.55 2.92-1.27 4.86-2.11 5.83-2.51 2.78-1.16 3.35-1.36 3.73-1.36.08 0 .27.02.39.12.1.08.13.19.14.27-.01.06.01.24 0 .38z"/></svg>`,
    building: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><rect x="4" y="2" width="16" height="20" rx="2" ry="2"></rect><line x1="9" y1="22" x2="9" y2="22.01"></line><line x1="15" y1="22" x2="15" y2="22.01"></line><line x1="9" y1="6" x2="9" y2="6.01"></line><line x1="15" y1="6" x2="15" y2="6.01"></line><line x1="9" y1="10" x2="9" y2="10.01"></line><line x1="15" y1="10" x2="15" y2="10.01"></line><line x1="9" y1="14" x2="9" y2="14.01"></line><line x1="15" y1="14" x2="15" y2="14.01"></line><line x1="9" y1="18" x2="9" y2="18.01"></line><line x1="15" y1="18" x2="15" y2="18.01"></line></svg>`,
    pin: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle></svg>`,
    globe: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"></circle><line x1="2" y1="12" x2="22" y2="12"></line><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path></svg>`,
    briefcase: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path></svg>`,
    check: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>`,
    alert: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>`,
    info: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>`,
    arrowRight: `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>`
};

// === Elementos do DOM ===
const form              = document.getElementById('search-form');
const btnBuscar         = document.getElementById('btn-buscar');
const inputKeyword      = document.getElementById('keyword');
const inputLocation     = document.getElementById('location');
const btnClearKeyword   = document.getElementById('btn-clear-keyword');

const groupModelo       = document.getElementById('group-modelo');
const groupTipo         = document.getElementById('group-tipo');

const stateIdle         = document.getElementById('state-idle');
const stateSkeleton     = document.getElementById('state-skeleton');
const stateError        = document.getElementById('state-error');
const stateEmpty        = document.getElementById('state-empty');
const errorMsg          = document.getElementById('error-msg');
const resultsSection    = document.getElementById('results-section');
const vagasGrid         = document.getElementById('vagas-grid');
const resultsCount      = document.getElementById('results-count');

const btnResetFilters   = document.getElementById('btn-reset-filters');
const btnRetry          = document.getElementById('btn-retry');

// Modal de Detalhes
const modalOverlay       = document.getElementById('modal-overlay');
const btnModalClose      = document.getElementById('btn-modal-close');
const modalTitle         = document.getElementById('modal-title');
const modalCompanyInfo   = document.getElementById('modal-company-info');
const modalMetaContainer = document.getElementById('modal-meta-container');
const modalDescricao     = document.getElementById('modal-descricao');
const modalLinkOrig      = document.getElementById('modal-link-original');
const btnEnviarTg        = document.getElementById('btn-enviar-telegram');

// Modal de Configurações
const modalConfig        = document.getElementById('modal-config');
const btnOpenConfig      = document.getElementById('btn-open-config');
const btnModalConfigClose= document.getElementById('btn-modal-config-close');
const formTelegramConfig = document.getElementById('form-telegram-config');
const tgBotTokenInput    = document.getElementById('tg-bot-token');
const tgChatIdInput      = document.getElementById('tg-chat-id');
const btnToggleToken     = document.getElementById('btn-toggle-token');
const btnClearTgConfig   = document.getElementById('btn-clear-tg-config');
const configBadgeDot     = document.getElementById('config-badge-dot');

const toast              = document.getElementById('toast');

// === Utilitários ===

function mostrarEstado(estado) {
    [stateIdle, stateSkeleton, stateError, stateEmpty, resultsSection]
        .forEach(el => {
            if (el) el.classList.add('hidden');
        });
    if (estado) estado.classList.remove('hidden');
}

function mostrarToast(mensagem, tipo = 'info', duracao = 3500) {
    const icon = tipo === 'sucesso' ? SVGS.check : (tipo === 'erro' ? SVGS.alert : SVGS.info);
    toast.innerHTML = `${icon}<span>${escaparHTML(mensagem)}</span>`;
    toast.className = `toast ${tipo}`;
    toast.classList.remove('hidden');

    if (toast._timer) clearTimeout(toast._timer);
    toast._timer = setTimeout(() => toast.classList.add('hidden'), duracao);
}

function escaparHTML(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function extrairIniciais(nome) {
    if (!nome) return 'VM';
    const partes = nome.trim().split(/\s+/);
    if (partes.length === 1) return partes[0].substring(0, 2).toUpperCase();
    return (partes[0][0] + partes[partes.length - 1][0]).toUpperCase();
}

// === Gerenciamento de Credenciais Locais do Telegram ===

function obterCredenciaisTelegram() {
    return {
        botToken: localStorage.getItem('vagamonitor_tg_token') || '',
        chatId: localStorage.getItem('vagamonitor_tg_chat_id') || ''
    };
}

function atualizarIndicadorTelegram() {
    const { botToken, chatId } = obterCredenciaisTelegram();
    if (botToken && chatId) {
        configBadgeDot.classList.remove('hidden');
        btnOpenConfig.setAttribute('title', 'Telegram configurado no navegador');
    } else {
        configBadgeDot.classList.add('hidden');
        btnOpenConfig.setAttribute('title', 'Configurar Telegram');
    }
}

// === Interatividade dos Filter Chips (Name That UI) ===

function inicializarChips() {
    // Grupo Modelo
    groupModelo.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            groupModelo.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            filtroModeloAtual = chip.dataset.model || '';
            if (inputKeyword.value.trim().length >= 2) {
                executarBusca();
            }
        });
    });

    // Grupo Tipo
    groupTipo.querySelectorAll('.chip').forEach(chip => {
        chip.addEventListener('click', () => {
            groupTipo.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            filtroTipoAtual = chip.dataset.type || '';
            if (inputKeyword.value.trim().length >= 2) {
                executarBusca();
            }
        });
    });
}

// Botões de sugestão rápida no Zero State
document.querySelectorAll('.quick-tag').forEach(tag => {
    tag.addEventListener('click', () => {
        const termo = tag.dataset.tag;
        inputKeyword.value = termo;
        inputKeyword.focus();
        atualizarBotaoLimpar();
        executarBusca();
    });
});

// Botão de limpar busca
inputKeyword.addEventListener('input', atualizarBotaoLimpar);

function atualizarBotaoLimpar() {
    if (inputKeyword.value.length > 0) {
        btnClearKeyword.style.display = 'block';
    } else {
        btnClearKeyword.style.display = 'none';
    }
}

btnClearKeyword.addEventListener('click', () => {
    inputKeyword.value = '';
    atualizarBotaoLimpar();
    inputKeyword.focus();
});

// Botão de reset de filtros no Empty State
if (btnResetFilters) {
    btnResetFilters.addEventListener('click', () => {
        inputKeyword.value = '';
        inputLocation.value = '';
        filtroModeloAtual = '';
        filtroTipoAtual = '';
        groupModelo.querySelectorAll('.chip').forEach(c => c.classList.toggle('active', c.dataset.model === ''));
        groupTipo.querySelectorAll('.chip').forEach(c => c.classList.toggle('active', c.dataset.type === ''));
        atualizarBotaoLimpar();
        mostrarEstado(stateIdle);
        inputKeyword.focus();
    });
}

if (btnRetry) {
    btnRetry.addEventListener('click', executarBusca);
}

// === Submissão da Busca ===

form.addEventListener('submit', (e) => {
    e.preventDefault();
    executarBusca();
});

async function executarBusca() {
    const palavraChave = inputKeyword.value.trim();
    const localizacao  = inputLocation.value.trim();

    if (!palavraChave) {
        inputKeyword.focus();
        mostrarToast('Informe uma palavra-chave para buscar vagas.', 'info');
        return;
    }

    mostrarEstado(stateSkeleton);
    btnBuscar.disabled = true;
    vagasAtual = [];

    try {
        const body = {
            palavraChave,
            localizacao: localizacao || null,
            modelo: filtroModeloAtual || null,
            tipo: filtroTipoAtual || null
        };

        const resp = await fetch('/api/vagas/buscar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        const data = await resp.json();

        if (!resp.ok) {
            const msg = data?.mensagem || `Erro ${resp.status}: fonte indisponível.`;
            errorMsg.textContent = msg;
            mostrarEstado(stateError);
            return;
        }

        const vagas = data.vagas ?? [];
        vagasAtual = vagas;

        if (vagas.length === 0) {
            mostrarEstado(stateEmpty);
            return;
        }

        renderizarVagas(vagas);
        mostrarEstado(resultsSection);

        if (data.aviso) mostrarToast(data.aviso, 'info');

    } catch (err) {
        console.error('[VagaMonitor] Falha de requisição:', err);
        errorMsg.textContent = 'Erro de conexão com o servidor. Verifique sua conexão e tente novamente.';
        mostrarEstado(stateError);
    } finally {
        btnBuscar.disabled = false;
    }
}

// === Renderização dos Cards de Vagas (Card Anatomy: Name That UI) ===

function renderizarVagas(vagas) {
    resultsCount.textContent = `${vagas.length} vaga${vagas.length !== 1 ? 's' : ''} encontrada${vagas.length !== 1 ? 's' : ''}`;
    vagasGrid.innerHTML = '';

    vagas.forEach((vaga, idx) => {
        const card = document.createElement('article');
        card.className = 'vaga-card';
        card.setAttribute('role', 'button');
        card.setAttribute('tabindex', '0');
        card.setAttribute('aria-label', `Ver detalhes da vaga: ${vaga.titulo}`);
        card.dataset.idx = idx;

        const empresa = vaga.empresa || 'Empresa parceira';
        const local   = vaga.localizacao || 'Brasil';
        const iniciais = extrairIniciais(empresa);

        // Identifica se é Remoto ou Híbrido para estilizar o pill
        const isRemoto = local.toLowerCase().includes('remoto');
        const isHibrido = local.toLowerCase().includes('híbrido') || local.toLowerCase().includes('hibrido');
        const pillClass = isRemoto ? 'pill-remote' : (isHibrido ? 'pill-hybrid' : '');
        const pillIcon = isRemoto ? SVGS.globe : SVGS.pin;

        const descricaoResumo = vaga.descricao
            ? escaparHTML(vaga.descricao)
            : 'Clique em "Ver detalhes" para consultar requisitos completos e informações da candidatura.';

        card.innerHTML = `
            <div>
                <div class="card-top">
                    <div class="company-avatar" aria-hidden="true">${iniciais}</div>
                    <div class="card-title-group">
                        <h3 class="vaga-titulo">${escaparHTML(vaga.titulo)}</h3>
                        <div class="company-name">
                            ${SVGS.building}
                            <span>${escaparHTML(empresa)}</span>
                        </div>
                    </div>
                </div>

                <div class="vaga-meta-row">
                    <span class="meta-pill ${pillClass}">
                        ${pillIcon}
                        <span>${escaparHTML(local)}</span>
                    </span>
                    <span class="meta-pill">
                        ${SVGS.briefcase}
                        <span>${escaparHTML(vaga.fonte || 'Gupy')}</span>
                    </span>
                </div>

                <p class="vaga-descricao-resumo">${descricaoResumo}</p>
            </div>

            <div class="card-actions">
                <button type="button" class="btn-card-details btn-ver" data-idx="${idx}">
                    <span>Ver detalhes</span>
                    ${SVGS.arrowRight}
                </button>
                <button type="button" class="btn-tg-action btn-tg-card" data-idx="${idx}" title="Enviar diretamente para o Telegram" aria-label="Enviar ao Telegram">
                    ${SVGS.telegram}
                </button>
            </div>
        `;

        vagasGrid.appendChild(card);
    });

    // Delegação de Eventos na Grade
    vagasGrid.onclick = (e) => {
        const btnTg = e.target.closest('.btn-tg-card');
        const btnVer = e.target.closest('.btn-ver');
        const card = e.target.closest('.vaga-card');

        if (btnTg) {
            e.stopPropagation();
            const idx = parseInt(btnTg.dataset.idx);
            enviarVagaAoTelegram(vagasAtual[idx]);
            return;
        }

        if (card || btnVer) {
            const idx = parseInt((card || btnVer).dataset.idx);
            abrirModalDetalhes(idx);
        }
    };

    vagasGrid.onkeydown = (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
            const card = e.target.closest('.vaga-card');
            if (card) {
                e.preventDefault();
                abrirModalDetalhes(parseInt(card.dataset.idx));
            }
        }
    };
}

// === Modal de Detalhes da Vaga (Dialog vs Drawer: Name That UI) ===

function abrirModalDetalhes(idx) {
    vagaSelecionada = vagasAtual[idx];
    if (!vagaSelecionada) return;

    modalTitle.textContent = vagaSelecionada.titulo || 'Sem título';
    modalCompanyInfo.innerHTML = `${SVGS.building} <span>${escaparHTML(vagaSelecionada.empresa || 'Empresa parceira')}</span>`;

    // Metadados
    const isRemoto = (vagaSelecionada.localizacao || '').toLowerCase().includes('remoto');
    const pillIcon = isRemoto ? SVGS.globe : SVGS.pin;
    const pillClass = isRemoto ? 'pill-remote' : '';

    modalMetaContainer.innerHTML = `
        <span class="meta-pill ${pillClass}">
            ${pillIcon}
            <span>${escaparHTML(vagaSelecionada.localizacao || 'Brasil')}</span>
        </span>
        <span class="meta-pill">
            ${SVGS.briefcase}
            <span>Portal Gupy</span>
        </span>
    `;

    modalDescricao.textContent = vagaSelecionada.descricao || 'Descrição detalhada disponível diretamente no link oficial da Gupy.';
    modalLinkOrig.href = vagaSelecionada.urlOriginal || '#';

    modalOverlay.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    btnModalClose.focus();
}

btnModalClose.addEventListener('click', fecharModalDetalhes);

function fecharModalDetalhes() {
    modalOverlay.classList.add('hidden');
    document.body.style.overflow = '';
}

modalOverlay.addEventListener('click', (e) => {
    if (e.target === modalOverlay) fecharModalDetalhes();
});

btnEnviarTg.addEventListener('click', () => {
    if (vagaSelecionada) {
        enviarVagaAoTelegram(vagaSelecionada);
    }
});

// === Disparo de Notificação ao Telegram ===

async function enviarVagaAoTelegram(vaga) {
    if (!vaga || !vaga.id) return;

    mostrarToast('Enviando oportunidade para o Telegram...', 'info', 2000);

    const { botToken, chatId } = obterCredenciaisTelegram();
    const payload = {};
    if (botToken) payload.botToken = botToken;
    if (chatId) payload.chatId = chatId;

    try {
        const resp = await fetch(`/api/vagas/${vaga.id}/telegram`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await resp.json();

        if (resp.ok && data.sucesso) {
            mostrarToast('Vaga enviada ao Telegram com sucesso!', 'sucesso');
        } else if (resp.status === 429) {
            mostrarToast(data.mensagem || 'Limite de envios atingido. Aguarde alguns minutos.', 'erro', 5000);
        } else {
            mostrarToast(data.mensagem || 'Falha ao enviar para o Telegram.', 'erro', 5000);
            if (!botToken || !chatId) {
                abrirModalConfig();
            }
        }
    } catch (err) {
        console.error('[VagaMonitor] Erro Telegram:', err);
        mostrarToast('Erro de comunicação com o serviço do Telegram.', 'erro');
    }
}

// === Modal de Configurações do Telegram ===

function abrirModalConfig() {
    const { botToken, chatId } = obterCredenciaisTelegram();
    tgBotTokenInput.value = botToken;
    tgChatIdInput.value = chatId;
    modalConfig.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
    tgBotTokenInput.focus();
}

function fecharModalConfig() {
    modalConfig.classList.add('hidden');
    document.body.style.overflow = '';
}

btnOpenConfig.addEventListener('click', abrirModalConfig);
btnModalConfigClose.addEventListener('click', fecharModalConfig);
modalConfig.addEventListener('click', (e) => {
    if (e.target === modalConfig) fecharModalConfig();
});

// Alternar visibilidade da senha do token
btnToggleToken.addEventListener('click', () => {
    const isPassword = tgBotTokenInput.type === 'password';
    tgBotTokenInput.type = isPassword ? 'text' : 'password';
    btnToggleToken.innerHTML = isPassword
        ? `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`
        : `<svg class="svg-icon svg-icon-sm" viewBox="0 0 24 24"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
});

formTelegramConfig.addEventListener('submit', (e) => {
    e.preventDefault();
    const token = tgBotTokenInput.value.trim();
    const chat = tgChatIdInput.value.trim();

    if (token) localStorage.setItem('vagamonitor_tg_token', token);
    else localStorage.removeItem('vagamonitor_tg_token');

    if (chat) localStorage.setItem('vagamonitor_tg_chat_id', chat);
    else localStorage.removeItem('vagamonitor_tg_chat_id');

    atualizarIndicadorTelegram();
    fecharModalConfig();
    mostrarToast('Configurações do Telegram salvas com sucesso!', 'sucesso');
});

btnClearTgConfig.addEventListener('click', () => {
    localStorage.removeItem('vagamonitor_tg_token');
    localStorage.removeItem('vagamonitor_tg_chat_id');
    tgBotTokenInput.value = '';
    tgChatIdInput.value = '';
    atualizarIndicadorTelegram();
    fecharModalConfig();
    mostrarToast('Credenciais removidas do navegador.', 'info');
});

// === Atalhos de Teclado & Acessibilidade Global ===
window.addEventListener('keydown', (e) => {
    // Tecla '/' para focar na busca rapidamente
    if (e.key === '/' && document.activeElement !== inputKeyword && document.activeElement !== inputLocation) {
        if (modalOverlay.classList.contains('hidden') && modalConfig.classList.contains('hidden')) {
            e.preventDefault();
            inputKeyword.focus();
            inputKeyword.select();
        }
    }

    // Tecla 'Escape' para fechar qualquer modal aberto
    if (e.key === 'Escape') {
        if (!modalOverlay.classList.contains('hidden')) fecharModalDetalhes();
        if (!modalConfig.classList.contains('hidden')) fecharModalConfig();
    }
});

// === Inicialização ===
inicializarChips();
atualizarIndicadorTelegram();
