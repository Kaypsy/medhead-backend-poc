package com.medhead.bedallocation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.annotation.security.RolesAllowed;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration OpenAPI/Swagger pour l'application.
 */
@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearer-jwt";

    @Bean
    public OpenAPI medHeadOpenAPI() {
        // Informations générales
        Info info = new Info()
                .title("MedHead - API d'Allocation de Lits d'Urgence")
                .description("""
                        API permettant l'allocation de lits d'urgence pour les hôpitaux, 
                        incluant la recherche des établissements les plus proches selon une spécialité,
                        la gestion des hôpitaux et des disponibilités. 
                        La sécurité repose sur des jetons Bearer JWT.
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("MedHead")
                        .url("https://www.medhead.com")
                        .email("support@medhead.com"));

        // Déclaration du Security Scheme (Bearer JWT)
        Components components = new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"));

        // Déclaration des serveurs (dev, prod)
        Server devServer = new Server()
                .url("http://localhost:8080/api")
                .description("Environnement de développement");
        Server prodServer = new Server()
                .url("https://api.medhead.com/api")
                .description("Environnement de production");

        // Tags globaux pour grouper les endpoints
        List<Tag> tags = Arrays.asList(
                new Tag().name("Hospitals").description("Endpoints de gestion et de recherche des hôpitaux")
        );

        return new OpenAPI()
                .info(info)
                .components(components)
                // Ne pas ajouter de SecurityRequirement global pour éviter le cadenas sur les endpoints publics;
                // le cadenas sera ajouté sélectivement via l'OperationCustomizer ci-dessous.
                .servers(List.of(devServer, prodServer))
                .tags(tags);
    }

    /**
     * Ajoute automatiquement l'exigence de sécurité (cadenas dans Swagger UI)
     * uniquement pour les endpoints protégés (annotés avec @PreAuthorize, @Secured, @RolesAllowed).
     */
    @Bean
    public OperationCustomizer protectSecuredOperations() {
        return (operation, handlerMethod) -> {
            if (isSecured(handlerMethod)) {
                operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
            }
            return operation;
        };
    }

    private boolean isSecured(HandlerMethod handlerMethod) {
        // Méthode
        boolean methodSecured = handlerMethod.hasMethodAnnotation(PreAuthorize.class)
                || handlerMethod.hasMethodAnnotation(Secured.class)
                || handlerMethod.hasMethodAnnotation(RolesAllowed.class);

        if (methodSecured) return true;

        // Classe contrôleur
        Class<?> beanType = handlerMethod.getBeanType();
        return beanType.isAnnotationPresent(PreAuthorize.class)
                || beanType.isAnnotationPresent(Secured.class)
                || beanType.isAnnotationPresent(RolesAllowed.class);
    }
}
