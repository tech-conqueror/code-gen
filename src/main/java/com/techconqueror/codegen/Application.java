package com.techconqueror.codegen;

import static com.techconqueror.codegen.generator.hibernate.HibernateEntityGenerator.*;

import java.sql.DriverManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner entityGeneratorRunner() {
        return args -> {
            var outputPath = "./src/main/java";

            var dbUrl = "jdbc:postgresql://34.87.149.96:32069/postgres";
            var username = "b049090";
            var password = "pwd";

            try (var connection = DriverManager.getConnection(dbUrl, username, password)) {
                generateEntitiesForAllTables(outputPath, connection);
            }
        };
    }
}
