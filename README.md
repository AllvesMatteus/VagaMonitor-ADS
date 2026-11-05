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
- [Java 21 LTS](https://learn.microsoft.com/pt-br/java/openjdk/download) (Eclipse Temurin, OpenJDK ou Oracle JDK 21) instalado e configurado nas variáveis de ambiente `JAVA_HOME` e `PATH` (verifique com `java -version`).
- [Apache Maven 3.9+](https://maven.apache.org/) instalado e configurado no `PATH` do sistema (verifique com `mvn -version`). O repositório não inclui o wrapper `mvnw`.
- Conexão ativa com a internet (necessária para que a aplicação consulte a API pública do Portal Gupy).

### 1. Obter o Código da Entrega
Como a branch padrão no GitHub é a `main`, o clone padrão posiciona o repositório nela. Para acessar a versão desta entrega, selecione a branch `master` ou a tag `v2` (após sua publicação):

```bash
git clone https://github.com/AllvesMatteus/VagaMonitor-ADS.git
cd VagaMonitor-ADS

# Selecionar a branch master da entrega:
git checkout master

# Ou selecionar diretamente a tag v2 da entrega:
git checkout v2
```

### 2. Configuração de Banco de Dados e Variáveis de Ambiente
- **Execução local padrão:** Não exige definir variáveis de ambiente no sistema operacional, desde que não existam variáveis de produção (como `DB_URL` ou `DATABASE_URL`) exportadas no terminal que substituam as configurações locais.
- **Banco H2 em arquivo:** A aplicação utiliza o H2 Database embutido gravado na pasta `./data/` (`./data/vagamonitor` no perfil padrão e `./data/vagamonitor-dev` no perfil dev). Os dados persistem entre reinicializações sem necessidade de instalar um servidor de banco de dados externo.
- **Migrações com Flyway:** O esquema, tabelas e índices são criados automaticamente na inicialização (`src/main/resources/db/migration/V1__init.sql`).
- **Diferença sobre arquivo .env:** O Spring Boot lê configurações de propriedades da JVM (`-D`) ou variáveis de ambiente do sistema operacional. O projeto não inclui biblioteca para carregamento automático de arquivos `.env`. O arquivo `.env.example` serve apenas como documentação de referência para variáveis aceitas em deploy de nuvem (Render/PostgreSQL).

### 3. Executar a Aplicação

#### Opção A: Perfil Padrão
Neste modo, o banco local utiliza o arquivo `./data/vagamonitor`. O console web do H2 permanece desabilitado por segurança.

```bash
mvn spring-boot:run
```

#### Opção B: Perfil de Desenvolvimento (dev)
Ativa logs em nível `DEBUG` para classes de negócio, exibição das consultas SQL no terminal e habilita o Console Web do H2.

```bash
# Qualquer sistema operacional (parâmetro Maven com aspas)
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"

# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run

# Windows (CMD)
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Linux / macOS (Bash)
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

### 4. Acessar no Navegador
- **Aplicação Web:** Acesse `http://localhost:8080`
- **Console do Banco H2 (disponível exclusivamente no perfil dev):**
  - URL de acesso: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/vagamonitor-dev`
  - Usuário: `sa`
  - Senha: (deixar em branco)

### 5. Executar Testes e Gerar o Pacote Executável
- **Executar a suíte de testes (12 testes unitários e de integração):**
```bash
mvn test

# Para limpeza prévia e execução completa:
mvn clean test
```
- **Gerar o arquivo JAR executável:**
```bash
mvn clean package
```
  O artefato é gerado em `target/vagamonitor-0.1.0-SNAPSHOT.jar`.
- **Executar a aplicação via JAR:**
```bash
java -jar target/vagamonitor-0.1.0-SNAPSHOT.jar
```

---

## 📖 Como Utilizar o Sistema

### Uso Sem Telegram (Consulta e Filtros Livres)
A configuração do Telegram não é obrigatória para usar a aplicação. Sem o bot configurado, você pode:
- Buscar vagas em tempo real via API pública da Gupy (requer internet).
- Filtrar por Modelo de Trabalho (Remoto, Híbrido, Presencial).
- Filtrar por Tipo de Contratação (Estágio, Efetivo, Jovem Aprendiz, Temporário).
- Navegar pela paginação em lotes de 10 vagas.
- Abrir os detalhes da vaga para inspecionar requisitos e acessar o link oficial de candidatura no Portal Gupy.

Ao abrir o site pela primeira vez, basta clicar em **Configurar Depois** no modal para navegar livremente.

### Uso Com Telegram (Envio com 1 Clique)
Permite encaminhar o resumo da oportunidade selecionada diretamente para um chat do Telegram.

1. **Criar o Bot:** Abra a conversa com o **[@BotFather](https://t.me/BotFather)** no Telegram, envie `/newbot`, escolha nome e usuário, e copie o Token de Acesso HTTP fornecido.
2. **Iniciar o Bot (Obrigatório):** Abra a conversa com o seu bot recém-criado no Telegram e clique em **Começar** (ou **Start**). Esse passo é indispensável para que o Telegram autorize o envio de mensagens.
3. **Descobrir o Chat ID:** Envie qualquer mensagem para o bot **[@userinfobot](https://t.me/userinfobot)** e copie o seu ID numérico.
4. **Configurar na Interface (Armazenamento e Transmissão):**
   - No modal de configuração da aplicação, insira o Token e o Chat ID e clique em **Salvar e Conectar**.
   - Os valores são gravados no navegador via `localStorage` (em texto simples) para conveniência de preenchimento.
   - Ao clicar no botão de envio de uma vaga, o navegador transmite essas credenciais no corpo da requisição HTTP POST para o endpoint `/api/vagas/{id}/telegram`. O servidor utiliza as credenciais em memória para disparar a mensagem via Telegram Bot API e não armazena essas chaves no banco de dados.
5. **Configuração Alternativa via Servidor:**
   - Também é possível definir as variáveis de ambiente `TELEGRAM_BOT_TOKEN` e `TELEGRAM_CHAT_ID` no ambiente onde o servidor é executado.

### Passo a Passo de Consulta
1. Digite a palavra-chave no campo de busca ou clique em um atalho do Hub de Carreiras.
2. Refine a pesquisa com os filtros rápidos de Modelo e Contratação.
3. Clique em **Buscar Vagas** e navegue pelas páginas numéricas de resultados.
4. Clique em **Ver detalhes** no card para ler as informações completas.
5. Se o Telegram estiver configurado, clique no botão azul do **Telegram** para despachar a vaga; ou clique em **Ver no Portal Gupy** para se candidatar.

---

## 📜 Scripts & Comandos Úteis

| Comando | Descrição |
|---|---|
| `mvn spring-boot:run` | Inicia a aplicação no perfil padrão (porta 8080) |
| `mvn spring-boot:run "-Dspring-boot.run.profiles=dev"` | Inicia no perfil dev com logs DEBUG e console H2 |
| `mvn test` | Executa os 12 testes unitários e de integração |
| `mvn clean test` | Limpa compilações anteriores e executa os testes do zero |
| `mvn clean package` | Compila, testa e gera o arquivo target/vagamonitor-0.1.0-SNAPSHOT.jar |
| `java -jar target/vagamonitor-0.1.0-SNAPSHOT.jar` | Executa diretamente o artefato empacotado da aplicação |
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
