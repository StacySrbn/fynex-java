package org.example.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Fynex API")
                .version("1.0")
                .description("REST API for Fynex - Personal Finance Management Application\n\n" +
                    "Features:\n" +
                    "- Transaction management (CRUD, filtering, sorting)\n" +
                    "- Categories (system & custom)\n" +
                    "- Budgets with spending reports\n" +
                    "- Savings goals tracking\n" +
                    "- Recurring payments with auto-generation\n" +
                    "- CSV import / CSV & PDF export\n" +
                    "- Financial statistics & balance\n" +
                    "- Admin panel (user management, roles, blocking)")
                .contact(new Contact()
                    .name("Fynex Support")
                    .email("support@fynex.app"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Enter JWT token")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development"),
                new Server().url("http://app:8080").description("Docker Container")
            ));
    }
}
