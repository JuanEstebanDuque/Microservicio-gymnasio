package com.GymMicroservicio.api_gateway.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Filtro de agregacion: arma el panel de un miembro consultando en paralelo
 * a miembros, clase y entrenador, y responde un unico JSON. No enruta a ningun
 * servicio (no llama a chain.filter): la respuesta la escribe el propio filtro.
 */
@Component
public class ResumenMiembroFilter implements GatewayFilter {

    private final WebClient webClient;

    public ResumenMiembroFilter(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String miembroId = ServerWebExchangeUtils.getUriTemplateVariables(exchange).get("miembroId");

        return Mono.zip(
                        consultar(token, "http://miembros/api/entrenamiento/resumen/{id}", miembroId),
                        consultar(token, "http://clase/api/clase"),
                        consultar(token, "http://clase/api/ocupacion"),
                        consultar(token, "http://entrenador/api/entrenador"))
                .flatMap(t -> {
                    // Cada servicio ya responde JSON valido: se anida tal cual, sin deserializar.
                    String json = "{\"entrenamientoSemanal\":" + t.getT1()
                            + ",\"clases\":" + t.getT2()
                            + ",\"ocupacionEnVivo\":" + t.getT3()
                            + ",\"entrenadores\":" + t.getT4() + "}";
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    return response.writeWith(Mono.just(
                            response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8))));
                });
    }

    // Si un servicio falla o esta caido, su seccion queda en null y el resto del panel responde igual.
    private Mono<String> consultar(String token, String url, Object... vars) {
        return webClient.get().uri(url, vars)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .bodyToMono(String.class)
                .defaultIfEmpty("null")
                .onErrorReturn("null");
    }
}
