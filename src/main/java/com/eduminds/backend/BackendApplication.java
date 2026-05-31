package com.eduminds.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(BackendApplication.class, args);
        Environment env = ctx.getEnvironment();

        String port   = env.getProperty("server.port", "8082");
        String claude = env.getProperty("claude.api.key", "");

        System.out.println("""
                
                ╔══════════════════════════════════════════╗
                ║          🧠  SYNAPZ · Backend            ║
                ╠══════════════════════════════════════════╣
                ║  URL  : http://localhost:%s            ║
                ║  DB   : synapz_db (MariaDB)              ║
                ║  IA   : %s                     ║
                ╚══════════════════════════════════════════╝
                """.formatted(
                port,
                claude.isBlank() ? "❌ Clé Claude absente (mode dev)" : "✅ Claude API connectée"
        ));
    }
}
