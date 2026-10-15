<p align="center">
  <img src="src/main/resources/static/img/logo-mackenzie.png" alt="Mackenzie Logo" width="110" />
</p>

<h1 align="center">VagaMonitor</h1>

<p align="center">
  <b>Busca Inteligente de Vagas em Tempo Real & Automação via Telegram</b><br>
  <span>Projeto Acadêmico • Prática Profissional em Análise e Desenvolvimento de Sistemas (ADS) • 5º Semestre</span><br>
  <span><a href="https://www.mackenzie.br/">Universidade Presbiteriana Mackenzie</a> — Faculdade de Computação e Informática (FCI)</span>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21 LTS" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.2.5" />
  <img src="https://img.shields.io/badge/Telegram_Bot-API-229ED9?style=for-the-badge&logo=telegram&logoColor=white" alt="Telegram Bot API" />
  <img src="https://img.shields.io/badge/Portal_Gupy-API_Pública-0052CC?style=for-the-badge&logo=googlechrome&logoColor=white" alt="Portal Gupy" />
  <img src="https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway" />
  <img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Design-Mackenzie_Red_%23D50425-D50425?style=for-the-badge" alt="Mackenzie Red" />
</p>

---

## 📌 Sobre o Projeto

O **VagaMonitor** é uma plataforma web desenvolvida para monitorar, consultar e filtrar oportunidades reais de emprego e estágio no mercado brasileiro, conectando a base de dados pública do **Portal Gupy** diretamente ao chat do **Telegram** do usuário em apenas um clique.

Criado no âmbito da disciplina **Prática Profissional em Análise e Desenvolvimento de Sistemas (5º Semestre)** na **Universidade Presbiteriana Mackenzie**, o projeto tem como objetivo demonstrar na prática a engenharia de um sistema distribuído moderno: arquitetura robusta em camadas com **Spring Boot 3**, consumo resiliente de APIs REST externas, persistência e deduplicação de dados, interface web minimalista de alta ergonomia e integração com bots de mensageria instantânea.

---

## 🎓 Como o VagaMonitor Ajuda os Alunos?

A busca por vagas de estágio e primeiro emprego na área de tecnologia (e correlatas) costuma ser cansativa, dispersa e frustrante. O VagaMonitor foi concebido pensando nas dores reais do estudante universitário:

* **🚀 Economia Brutal de Tempo:** Elimina a necessidade de abrir múltiplos portais, passar por telas lentas de login ou navegar em dezenas de páginas pesadas. Tudo é consultado em tempo real em uma única tela leve.
* **📱 Oportunidades no Bolso via Telegram:** O aluno não precisa ficar vigiando o navegador. Ao encontrar uma vaga interessante, clica em *Enviar para o Telegram* e recebe o resumo com requisitos e link oficial direto no celular para se candidatar quando quiser.
* **🎯 Foco em Estágios e Início de Carreira:** Filtros dedicados para **Estágio** e **Jovem Aprendiz**, além de um **Hub de Carreiras** com atalhos para *Estágio em TI*, *Estágio em Administração*, *Desenvolvedor Júnior*, *Ciência de Dados* e *Suporte*.
* **🏠 Filtro de Modelo Remoto & Híbrido:** Essencial para quem estuda à noite ou longe dos centros corporativos e busca posições com flexibilidade de horário e localização.
* **🔒 Zero Burocracia & Privacidade Total:** Não exige criar conta, senha ou preencher cadastros invasivos. As credenciais do bot do Telegram ficam salvas com segurança no navegador do próprio estudante (`localStorage`).

---

## ⚙️ Como Funciona o Sistema?

O fluxo do VagaMonitor opera sob demanda (on-demand), garantindo que os dados exibidos ao usuário reflitam com precisão as vagas disponíveis no momento:

```
[ Usuário / Aluno ]
       │
       ▼ (1) Digita palavra-chave ou clica no Hub de Carreiras
[ Frontend Web (HTML5 / Vanilla CSS / JS) ]
       │
       ▼ (2) POST /api/vagas/buscar
[ Backend Spring Boot (VagaController / VagaService) ]
       │
       ├────────► (3) GupyClient consulta a API pública do Portal Gupy
       │              (com tradução de tipos, modelos e tratamento de erros)
       │
       ├────────► (4) Deduplica vagas pelo idExterno e persiste em H2/PostgreSQL
       │
       ▲ (5) Retorna listagem normalizada de oportunidades
       │
[ Frontend renderiza cards de vagas ]
       │
       ▼ (6) Usuário clica no botão "Telegram" na vaga desejada
[ Backend TelegramService ]
       │
       └────────► (7) Dispara mensagem formatada via Telegram Bot API
                      para o Chat ID configurado pelo aluno
```

---

## ✨ Principais Funcionalidades

### 🔍 Busca Inteligente & Hub de Carreiras
- **Consulta em Tempo Real:** Conexão direta com a API pública da Gupy trazendo vagas recém-publicadas no Brasil.
- **Hub de Especialidades (Zero State):** Na tela inicial, 4 pilares guiam o usuário com tags rápidas:
  - *Tecnologia & Dados* (Dev, Java, Python, Frontend, Dados, Redes)
  - *Negócios & Gestão* (Administrativo, Financeiro, RH, Contabilidade, Projetos)
  - *Comercial & Criativo* (Vendas, Marketing Digital, Design/UX, Social Media)
  - *Entrada no Mercado* (Estágio Geral, Estágio em TI, Jovem Aprendiz, Trainee)

### 📄 Paginação Inteligente de Vagas (Lotes de 10 em 10)
- **Consulta Integral sem Limite Rígido:** Integração com os parâmetros `limit` e `offset` da API da Gupy, buscando a totalidade de oportunidades disponíveis (sem ficar restrito a apenas 20 vagas).
- **Navegação Fluida (Páginas 1, 2, 3, 4...):** Exibição organizada em lotes de 10 vagas, com botões numéricos inteligentes, reticências automáticas, botões *Anterior* e *Próxima*, indicador do lote atual (`Mostrando vagas 1–10 de 472`) e rolagem suave ao alternar páginas.

### 🎯 Filtros Rápidos com Agrupamento Ergonômico Lado a Lado
- **Disposição Horizontal Otimizada:** Filtros de **Modelo** e **Contratação** alinhados lado a lado na mesma linha com divisor institucional sutil.
- **Modelo de Trabalho:** Todos, Remoto, Híbrido e Presencial.
- **Tipo de Contratação:** Todos, Efetivo (CLT), Estágio, Jovem Aprendiz e Temporário.
- **Filtro Combinado:** Permite pesquisar, por exemplo, *"Java"* apenas em regime *"Remoto"* e tipo *"Estágio"*.

### ⚡ Despacho com 1 Clique para o Telegram
- Envio instantâneo de vaga com dados completos (título, empresa contratante, modalidade, localidade e link de candidatura).
- Suporte a envio para **chats pessoais**, **grupos de estudo** ou **canais do Telegram**.
- Botão de envio rápido tanto na grade de cards quanto dentro do modal de detalhes da vaga.

### 🤖 Onboarding Intuitivo do Telegram
- **Abertura Automática:** Na primeira visita do usuário, o modal de configuração é aberto automaticamente com instruções didáticas em menos de 2 minutos.
- **Explicação Clara (O Porquê & O Como):** Guia passo a passo numerado explicando como usar o `@BotFather` e o `@userinfobot`.
- **Opção "Configurar Depois":** Não trava a navegação caso o usuário queira apenas consultar vagas antes de conectar o Telegram.

### 🎨 Design System Minimalista Mackenzie
- **Paleta Oficial:** Branco Puro (`#ffffff`), Vermelho Mackenzie (`#d50425`), ardósia neutra (*Slate*) e bordas quadradas modernas (`radius: 0px`).
- **Footer Escuro Acadêmico:** Rodapé robusto em tom escuro (`#0b1120`) contendo a identidade institucional, ficha acadêmica completa, stack de tecnologias e atalhos rápidos.
- **100% Responsivo:** Otimizado para monitores ultra-wide, laptops, tablets e smartphones estreitos (320px+).

### 🛡️ Resiliência & Proteção
- **Rate Limiting por IP:** Interceptor que protege o backend contra abusos (limite de 5 envios/hora por IP para requisições de mensageria).
- **Tratamento de Indisponibilidade:** Mensagens amigáveis caso a API externa sofra instabilidade temporária.
- **Deduplicação de Vagas:** Índice único `idx_vagas_id_externo` no banco impedindo duplicatas na persistência.

---

## 🛠️ Stack Tecnológico

| Camada | Tecnologias & Bibliotecas |
|---|---|
| **Backend** | Java 21 LTS, Spring Boot 3.2.5, Spring MVC, Spring Data JPA |
| **Banco de Dados** | H2 Database (desenvolvimento em arquivo local) e PostgreSQL 15+ (produção em nuvem) |
| **Migrações de Banco** | Flyway Migration (`V1__init.sql`) |
| **Integrações Externas** | API Pública Portal Gupy, Telegram Bot API via `RestTemplate` |
| **Frontend** | HTML5 Semântico, CSS3 Moderno (Vanilla com Design Tokens), JavaScript ES6+ Vanilla |
| **Identidade Visual** | Padrão Visual Universidade Presbiteriana Mackenzie (Pantone 185 CP / `#D50425`) |
| **Testes Automatizados** | JUnit 5, Mockito, Spring Boot Test (12 testes unitários e integrados) |
| **Deploy & Nuvem** | Render Cloud (Docker Web Service Linux Alpine com Eclipse Temurin OpenJDK 21) |

---

## 📂 Estrutura de Pastas

```
VagaMonitor-ADS/
├── data/                                 # Banco de dados H2 persistente local (dev)
├── docs/                                 # Documentação de projeto e Identidade Visual Mackenzie
│   ├── Identidade visual/               # Manual de aplicação e logos oficiais da Mackenzie
│   └── INDICE_ORGANIZACAO.md             # Índice de conformidade e entregas acadêmicas
├── src/
│   ├── main/
│   │   ├── java/com/vagamonitor/
│   │   │   ├── VagaMonitorApplication.java  # Classe principal Spring Boot
│   │   │   ├── config/                      # Configurações Web, DataSource e RateLimitInterceptor
│   │   │   ├── controller/                  # VagaController (endpoints REST)
│   │   │   ├── dto/                         # DTOs de Busca, Vaga e Resposta da API
│   │   │   ├── integration/                 # Clientes HTTP (GupyClient com headers e fallback)
│   │   │   ├── model/                       # Entidades JPA (Vaga, CriterioBusca)
│   │   │   ├── repository/                  # Repositórios Spring Data JPA
│   │   │   └── service/                     # Regras de negócio (VagaService, TelegramService)
│   │   └── resources/
│   │       ├── application.properties       # Configurações gerais da aplicação
│   │       ├── application-dev.properties   # Perfil local com banco H2
│   │       ├── application-test.properties  # Perfil de testes automatizados
│   │       ├── db/migration/V1__init.sql    # Migração Flyway de criação de tabelas
│   │       └── static/                      # Frontend da aplicação
│   │           ├── index.html               # Página única semântica da aplicação
│   │           ├── css/style.css            # Design System Mackenzie & responsividade
│   │           ├── js/app.js                # Lógica client-side, filtros, modais e Telegram
│   │           └── img/                     # Logos oficiais da Mackenzie (vermelha e branca)
│   └── test/java/com/vagamonitor/           # Suíte de testes automatizados com JUnit e Mockito
├── Dockerfile                            # Build de contêiner para deploy em nuvem
├── pom.xml                               # Gerenciamento de dependências Maven
└── README.md                             # Documentação oficial do projeto
```

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos
- [Java 21 LTS](https://learn.microsoft.com/pt-br/java/openjdk/download) instalado e configurado no `PATH`.
- [Apache Maven 3.9+](https://maven.apache.org/) (ou utilize o wrapper `./mvnw` incluso).

### 1. Clonar o Repositório
```bash
git clone https://github.com/AllvesMatteus/VagaMonitor-ADS.git
cd VagaMonitor-ADS
```

### 2. Executar a Aplicação
Execute o Maven com o perfil de desenvolvimento ativo:

```bash
# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run

# Windows (CMD)
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Linux / macOS (Bash)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

### 3. Acessar no Navegador
Abra o navegador e acesse:
```
http://localhost:8080
```

> **Console do Banco H2:** Para inspecionar as tabelas locais criadas pelo Flyway, acesse `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/vagamonitor`, Usuário: `sa`, Senha em branco).

---

## 📖 Como Utilizar o Sistema

### Passo 1: Configurar o Telegram (Leva menos de 2 minutos)
1. Ao abrir o site pela primeira vez, o modal de configuração aparecerá na tela.
2. No Telegram, abra a conversa com o bot oficial **[@BotFather](https://t.me/BotFather)**.
3. Envie o comando `/newbot`, escolha um nome e um username para o seu bot.
4. O BotFather fornecerá um **Token de Acesso HTTP** (ex: `123456789:ABCdefGhIJKlmNoPQRstuVWXyz`). Copie e cole no primeiro campo do modal.
5. Inicie seu bot recém-criado clicando em **Começar / Start**.
6. Para descobrir seu Chat ID numérico, mande uma mensagem para o bot **[@userinfobot](https://t.me/userinfobot)**. Copie seu ID (ex: `987654321`) e cole no segundo campo.
7. Clique em **Salvar e Conectar**. Pronto! *(Se quiser, você também pode clicar em "Configurar Depois" e fazer isso em outro momento).*

### Passo 2: Buscar Vagas
1. No campo de busca, digite o cargo ou tecnologia desejada (ex: `Java`, `Estágio`, `Python`, `Administrativo`).
2. Opcionalmente, informe uma localização (ex: `São Paulo`, `Remoto`).
3. Ou simplesmente clique em qualquer especialidade do **Hub de Carreiras** na tela inicial.
4. Refine sua busca com os filtros rápidos lado a lado de **Modelo** (Remoto / Híbrido / Presencial) e **Contratação** (Estágio / Efetivo / Jovem Aprendiz / Temporário).
5. Clique em **Buscar Vagas**.
6. Navegue facilmente pelos lotes de 10 vagas através da barra de paginação numérica (1, 2, 3, 4...).

### Passo 3: Consultar Detalhes e Enviar ao Telegram
1. Clique em **Ver detalhes** no card da vaga para inspecionar os requisitos completos.
2. Clique no botão azul do **Telegram** no card ou no modal: a oportunidade será enviada imediatamente para o seu chat!
3. Clique em **Ver no Portal Gupy** para acessar diretamente o link oficial de inscrição da empresa.

---

## 📜 Scripts & Comandos Úteis

| Comando | Descrição |
|---|---|
| `mvn spring-boot:run` | Inicia o servidor local do Spring Boot na porta 8080 |
| `mvn test` | Executa todos os 12 testes unitários e de integração |
| `mvn clean package` | Compila o projeto e gera o arquivo `.jar` executável em `target/` |
| `mvn flyway:info` | Exibe o status e histórico das migrações do banco de dados |

---

## 🚢 Publicação em Produção (Nuvem)

A aplicação está configurada para deploy automático em contêiner Linux no **Render Cloud**:

* **Ambiente de Produção:** [https://vagamonitor-ads.onrender.com/](https://vagamonitor-ads.onrender.com/)
* **Contêiner:** Docker multi-stage build com Eclipse Temurin OpenJDK 21 sobre Alpine Linux.
* **Banco de Dados em Nuvem:** PostgreSQL gerenciado com execução automática de migrações Flyway na inicialização.

---

## 👨‍🎓 Ficha Técnica Acadêmica

* **Instituição:** [Universidade Presbiteriana Mackenzie](https://www.mackenzie.br/)
* **Unidade Acadêmica:** Faculdade de Computação e Informática (FCI)
* **Curso:** Tecnologia em Análise e Desenvolvimento de Sistemas (ADS)
* **Período / Semestre:** 5º Semestre
* **Disciplina:** Prática Profissional em Análise e Desenvolvimento de Sistemas
* **Desenvolvedor:** Mateus Alves
* **Objetivo Acadêmico:** Aplicação integral de padrões arquiteturais em Java/Spring, consumo seguro de APIs externas, design system institucional e automação de mensageria voltada à inserção de estudantes no mercado de trabalho.

---

## 📄 Licença

Este projeto é desenvolvido para fins estritamente **acadêmicos e educacionais** no âmbito do curso de Análise e Desenvolvimento de Sistemas da Universidade Presbiteriana Mackenzie. Todos os direitos reservados aos respectivos autores e marcas citadas.
