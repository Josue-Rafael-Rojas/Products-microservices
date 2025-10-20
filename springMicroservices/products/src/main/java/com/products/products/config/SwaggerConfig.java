package com.products.products.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpHeaders;


@OpenAPIDefinition(
        info = @Info(
                title = "MICROSERVICIO PRODUCTS - LINKTIC",
                description = "Esta api fue desarrollada para la prueba tecnica enviada por linktic",
                version = "1.0.0",
                contact = @Contact(
                        name = "Josue Rojas",
                        email = "josuerafarojas09@gmail.com",
                        url = "https://www.linkedin.com/in/josue-rojas-backend-developer/"
                )
        ),
        servers = {@Server(
                description = "SERVIDOR DE DESARROLLO",
                url = "http://localhost:8080"
        )},
        security = @SecurityRequirement(
                name = "Token de seguridad"
        )
)
@SecurityScheme(
        name = "Token de seguridad",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = HttpHeaders.AUTHORIZATION,
        description = "Token basico tipo Bearer"
)
public class SwaggerConfig {
}
