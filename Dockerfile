# =======================================================
# Dockerfile para VagaMonitor (Spring Boot + Java 21)
# Otimizado para deploy no Render / Railway / Docker
# =======================================================

# Estágio 1: Compilação com Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache de dependências do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar o código-fonte da aplicação
COPY src ./src
RUN mvn clean package -DskipTests -B

# Estágio 2: Imagem final de execução (JRE leve Alpine)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Criar grupo, usuário e diretórios com permissões adequadas
RUN addgroup -S appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/data && chown -R appuser:appgroup /app

USER appuser:appgroup

# Copiar o JAR construído
COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

# Variável de porta (Render injeta dinamicamente)
EXPOSE 8080

# Limite de memória adequado ao plano gratuito do Render (512MB RAM)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
