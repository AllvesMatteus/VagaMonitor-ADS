package com.vagamonitor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Configuracao flexivel de DataSource para desenvolvimento local e nuvem (Render, Railway, Heroku).
 * Suporta automaticamente:
 * 1. URLs padrão de nuvem (postgres://user:pass@host:port/db ou postgresql://...)
 * 2. URLs JDBC padrão (jdbc:postgresql://... ou jdbc:h2:...)
 * 3. H2 local padrão (desenvolvimento e testes)
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Value("${DB_URL:#{null}}")
    private String dbUrl;

    @Value("${spring.datasource.url:jdbc:h2:file:./data/vagamonitor}")
    private String springDatasourceUrl;

    @Value("${spring.datasource.username:#{null}}")
    private String defaultUsername;

    @Value("${spring.datasource.password:#{null}}")
    private String defaultPassword;

    @Value("${spring.datasource.driver-class-name:#{null}}")
    private String defaultDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = (databaseUrl != null && !databaseUrl.isBlank()) 
                ? databaseUrl 
                : ((dbUrl != null && !dbUrl.isBlank()) ? dbUrl : springDatasourceUrl);

        HikariConfig config = new HikariConfig();

        // Tratamento automatico para strings de conexao fornecidas por Render / Heroku (postgres:// ou postgresql:// sem jdbc:)
        if (rawUrl != null && (rawUrl.startsWith("postgres://") || (rawUrl.startsWith("postgresql://") && !rawUrl.startsWith("jdbc:")))) {
            log.info("Detectada URL de banco de dados na nuvem (Render/Railway). Normalizando para JDBC...");
            try {
                String uriString = rawUrl.startsWith("postgres://")
                        ? "postgresql://" + rawUrl.substring("postgres://".length())
                        : rawUrl;

                URI uri = new URI(uriString);
                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // inclui '/' inicial
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                config.setJdbcUrl(jdbcUrl);
                config.setDriverClassName("org.postgresql.Driver");

                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    config.setUsername(userParts[0]);
                    if (userParts.length > 1) {
                        config.setPassword(userParts[1]);
                    }
                } else {
                    if (defaultUsername != null) config.setUsername(defaultUsername);
                    if (defaultPassword != null) config.setPassword(defaultPassword);
                }

                log.info("Conexao PostgreSQL configurada para: jdbc:postgresql://{}:{}{}", host, port, path);
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.error("Falha ao analisar URL do PostgreSQL: {}. Utilizando fallback direto.", e.getMessage());
            }
        }

        // Caso seja H2 em arquivo, assegura que o diretorio pai existe
        if (rawUrl != null && rawUrl.startsWith("jdbc:h2:file:")) {
            try {
                String filePath = rawUrl.substring("jdbc:h2:file:".length());
                if (filePath.contains(";")) {
                    filePath = filePath.substring(0, filePath.indexOf(";"));
                }
                java.io.File dbFile = new java.io.File(filePath);
                java.io.File parent = dbFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    boolean created = parent.mkdirs();
                    if (!created && !parent.exists()) {
                        log.warn("Nao foi possivel criar diretorio {}. Alternando para H2 em memoria.", parent);
                        rawUrl = "jdbc:h2:mem:vagamonitor;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
                    }
                }
            } catch (Exception ex) {
                log.warn("Falha ao checar diretorio H2: {}. Alternando para H2 em memoria.", ex.getMessage());
                rawUrl = "jdbc:h2:mem:vagamonitor;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
            }
        }

        // Conexao JDBC direta (H2 ou PostgreSQL já formatado com jdbc:...)
        config.setJdbcUrl(rawUrl);
        if (defaultDriver != null && !defaultDriver.isBlank()) {
            config.setDriverClassName(defaultDriver);
        } else if (rawUrl != null && rawUrl.startsWith("jdbc:postgresql:")) {
            config.setDriverClassName("org.postgresql.Driver");
        } else {
            config.setDriverClassName("org.h2.Driver");
        }

        if (defaultUsername != null) {
            config.setUsername(defaultUsername);
        }
        if (defaultPassword != null) {
            config.setPassword(defaultPassword);
        }

        try {
            return new HikariDataSource(config);
        } catch (Exception e) {
            if (rawUrl != null && rawUrl.startsWith("jdbc:h2:file:")) {
                log.warn("Falha ao inicializar H2 em arquivo (permissao/disco): {}. Recuperando com H2 em memoria...", e.getMessage());
                config.setJdbcUrl("jdbc:h2:mem:vagamonitor;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
                return new HikariDataSource(config);
            }
            throw e;
        }
    }
}
