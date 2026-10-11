package com.GymMicroservicio.api_gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;

import com.GymMicroservicio.api_gateway.filter.ResumenMiembroFilter;

@Configuration
public class RutasConfig {

    // WebClient que resuelve nombres de servicio (http://miembros/...) con Eureka + LoadBalancer.
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RouteLocator rutas(RouteLocatorBuilder builder, ResumenMiembroFilter resumenMiembroFilter) {
        return builder.routes()
                .route("entrenadores", r -> r.path("/api/entrenador/**")
                        .uri("lb://entrenador"))
                .route("resumen-miembro", r -> r.path("/api/resumen/miembro/{miembroId:[0-9]+}")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.filter(resumenMiembroFilter))
                        .uri("no://op"))
                .build();
    }
}
