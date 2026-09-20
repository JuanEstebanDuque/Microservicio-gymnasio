package co.analisys.clase.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Cliente hacia el microservicio de Entrenador. Reenvia el JWT del usuario que hizo
     * la peticion original (la llamada es sincrona, asi que el token esta disponible).
     */
    @Bean
    public RestClient entrenadorRestClient(@Value("${entrenador.service.url}") String entrenadorServiceUrl) {
        return RestClient.builder()
                .baseUrl(entrenadorServiceUrl)
                .requestInterceptor((request, body, execution) -> {
                    if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt) {
                        request.getHeaders().setBearerAuth(jwt.getToken().getTokenValue());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
