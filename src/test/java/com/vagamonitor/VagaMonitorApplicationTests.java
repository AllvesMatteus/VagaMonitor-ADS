package com.vagamonitor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class VagaMonitorApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe sem erros
    }
}
