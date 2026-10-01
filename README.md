# VagaMonitor — Sistema de Monitoramento de Vagas de Emprego

> **Projeto Acadêmico** — Prática Profissional em ADS  
> Universidade Presbiteriana Mackenzie • 5° Semestre

Aplicação web Java/Spring Boot que permite buscar vagas de emprego **sob demanda** em tempo real e enviá-las diretamente para um chat do **Telegram** com um clique.

---

## ✨ Funcionalidades (v0.1)

| Funcionalidade | Status |
|---|---|
| Busca de vagas por palavra-chave | ✅ Implementado |
| Busca com filtro de localização | ✅ Implementado |
| Exibição de título, empresa, localização e link | ✅ Implementado |
| Modal de detalhes da vaga | ✅ Implementado |
| Envio de vaga para Telegram (por botão) | ✅ Implementado |
| Persistência de critérios e vagas no banco | ✅ Implementado |
| Deduplicação de vagas por URL | ✅ Implementado |
| Rate limiting (5 envios/hora por IP) | ✅ Implementado |
| Feedback de erro (fonte indisponível, Telegram não configurado) | ✅ Implementado |

**Não implementado nesta versão:** cadastro/login, agendamento periódico, e-mail, envio automático de vagas.

---

## 🌐 Fonte de Vagas

**[Portal Gupy](https://portal.gupy.io)**

- Endpoint público de busca de vagas em tempo real no mercado de trabalho brasileiro
- Acesso a oportunidades reais em milhares de empresas (presencial, híbrido e remoto)
- Endpoint: `GET https://portal.gupy.io/api/job-search/jobs?jobName={palavraChave}&limit=20`
- Suporta filtros por palavra-chave, localização/estado, modelo de trabalho (remoto, híbrido, presencial) e tipo de contratação
- Campos mapeados: título (`name`), empresa (`careerPageName`), localização (`city`, `state`, `workplaceType`), link oficial de candidatura (`jobUrl`) e descrição resumida


---

## 🚀 Como Rodar Localmente

### Pré-requisitos
- Java 21+ ([Microsoft OpenJDK 21](https://learn.microsoft.com/pt-br/java/openjdk/download))
- Maven 3.9+ (ou use o wrapper: `./mvnw`)

### Passos

```bash
# 1. Clone o repositório
git clone https://github.com/AllvesMatteus/VagaMonitor-ADS.git
cd VagaMonitor-ADS

# 2. (Opcional) Configure o Telegram — copie e preencha
cp .env.example .env

# 3. Rode com perfil de desenvolvimento (H2 em arquivo, persiste após reiniciar)
set SPRING_PROFILES_ACTIVE=dev    # Windows CMD
# ou
$env:SPRING_PROFILES_ACTIVE="dev"  # PowerShell

mvn spring-boot:run

# 4. Acesse no navegador
# http://localhost:8080
# http://localhost:8080/h2-console  (console do banco H2)
```

### Com Telegram configurado

```powershell
$env:TELEGRAM_BOT_TOKEN="seu_token"
$env:TELEGRAM_CHAT_ID="seu_chat_id"
mvn spring-boot:run
```

---

## 🗄️ Banco de Dados

### Desenvolvimento (padrão)
Arquivo H2 em `./data/vagamonitor-dev.mv.db` — **persiste após reiniciar**.

```
JDBC URL:  jdbc:h2:file:./data/vagamonitor-dev
Usuário:   sa
Senha:     (vazio)
```

### Produção (PostgreSQL)

Defina as variáveis de ambiente:

```env
DB_URL=jdbc:postgresql://host:5432/vagamonitor
DB_USER=postgres
DB_PASSWORD=suasenha
DB_DRIVER=org.postgresql.Driver
```

### Consultas SQL para verificar persistência

```sql
-- Critérios buscados
SELECT * FROM criterios_busca ORDER BY buscado_em DESC LIMIT 10;

-- Vagas gravadas
SELECT id, titulo, empresa, localizacao, fonte, criado_em 
FROM vagas ORDER BY criado_em DESC LIMIT 10;

-- Total de vagas por fonte
SELECT fonte, COUNT(*) as total FROM vagas GROUP BY fonte;

-- Verificar deduplicação
SELECT id_externo, COUNT(*) FROM vagas GROUP BY id_externo HAVING COUNT(*) > 1;
```

---

## 🔐 Variáveis de Ambiente

| Variável | Obrigatória | Descrição |
|---|---|---|
| `TELEGRAM_BOT_TOKEN` | Não* | Token do bot Telegram |
| `TELEGRAM_CHAT_ID` | Não* | ID do chat de destino |
| `DB_URL` | Não (dev) | URL JDBC do banco de dados |
| `DB_USER` | Não (dev) | Usuário do banco |
| `DB_PASSWORD` | Não (dev) | Senha do banco |

*Sem o token, o botão Telegram exibe mensagem de erro clara na interface.

---

## 🏗️ Arquitetura

```
src/main/java/com/vagamonitor/
├── controller/VagaController.java      # REST endpoints
├── service/
│   ├── VagaService.java                # Lógica de negócio + persistência
│   └── TelegramService.java            # Envio via Bot API
├── integration/ArbeitNowClient.java    # Cliente da fonte externa
├── model/
│   ├── Vaga.java                       # Entidade JPA
│   └── CriterioBusca.java              # Entidade JPA
├── repository/
│   ├── VagaRepository.java
│   └── CriterioBuscaRepository.java
├── dto/                                # VagaDTO, BuscaDTO, RespostaDTO
└── config/
    ├── WebConfig.java                  # RestTemplate, interceptors
    └── RateLimitInterceptor.java       # Rate limit 5/hora por IP
```

### Endpoints REST

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/vagas/buscar` | Busca vagas na API externa e persiste |
| `GET` | `/api/vagas/{id}` | Detalhes de uma vaga pelo ID |
| `POST` | `/api/vagas/{id}/telegram` | Envia vaga para o Telegram (rate limited) |

---

## 🛡️ Proteção contra Abuso

- **Rate limiting por IP:** máximo de **5 envios por hora** para o Telegram
- Chaves do Telegram **nunca expostas** na interface, em logs ou no Git
- Erros de configuração informados ao usuário na interface

---

## 🚢 Publicação (Instruções Preparadas — Deploy Pendente)

### Opção A: Railway (recomendado para MVP)

1. Crie conta em [railway.app](https://railway.app)
2. New Project → Deploy from GitHub → selecione este repositório
3. Adicione serviço PostgreSQL no mesmo projeto
4. Configure as variáveis de ambiente no painel Railway:
   - `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHAT_ID`
   - `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_DRIVER`
   - `SPRING_PROFILES_ACTIVE=prod`

### Opção B: Render

1. New Web Service → conecte o repositório
2. Build command: `mvn package -DskipTests`
3. Start command: `java -jar target/vagamonitor-0.1.0-SNAPSHOT.jar`
4. Adicione Render PostgreSQL e configure as variáveis

### Opção C: Docker

```dockerfile
FROM eclipse-temurin:21-jre
COPY target/vagamonitor-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

---

## 🧪 Testes

```bash
# Rodar todos os testes
mvn test

# Os testes do TelegramService e ArbeitNowClient usam WireMock
# (nenhuma chamada real é feita durante os testes)
```

---

## ⚠️ Limitações Conhecidas

- A Arbeitnow API retorna principalmente vagas em inglês/Europa; não há API pública BR gratuita sem cadastro
- O filtro de localização é aplicado localmente (pós-busca)
- Rate limit armazenado em memória (reiniciar a aplicação reseta os contadores)
- H2 em arquivo não é recomendado para produção; use PostgreSQL
