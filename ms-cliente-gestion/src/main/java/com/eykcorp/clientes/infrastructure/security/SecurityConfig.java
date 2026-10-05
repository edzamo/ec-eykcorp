package com.eykcorp.clientes.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

/**
 * API stateless con bearer JWT (HS256): CSRF deshabilitado, sin sesión, sin httpBasic ni formLogin.
 * Son públicos {@code POST /auth/login}, {@code GET /actuator/health} y la documentación
 * (Swagger UI y contrato OpenAPI, solo GET); {@code /clientes/**} exige autenticación y cualquier
 * otra ruta se deniega.
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties({JwtProperties.class, AdminProperties.class})
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain cadenaDeSeguridad(
            ServerHttpSecurity http, ReactiveJwtDecoder decodificador, ProblemaSeguridadHandler problemas) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(rutas -> rutas
                        .pathMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        // Documentación pública (trade-off asumido): el contrato OpenAPI y Swagger UI se
                        // sirven sin token para poder consultarlos y obtener el JWT desde la propia UI.
                        // Solo GET y solo estas rutas (swagger-config es la configuración de la UI; el
                        // /v3/api-docs generado desde el código sigue denegado); no expone datos ni operaciones.
                        .pathMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/webjars/**",
                                "/openapi/**", "/v3/api-docs/swagger-config").permitAll()
                        .pathMatchers("/clientes/**").authenticated()
                        .anyExchange().denyAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(problemas).accessDeniedHandler(problemas))
                .oauth2ResourceServer(o -> o
                        .authenticationEntryPoint(problemas)
                        .accessDeniedHandler(problemas)
                        .jwt(jwt -> jwt.jwtDecoder(decodificador)))
                .build();
    }

    @Bean
    ProblemaSeguridadHandler problemaSeguridadHandler(ObjectMapper objectMapper) {
        return new ProblemaSeguridadHandler(objectMapper);
    }

    /** Valida firma HS256, {@code exp} (obligatorio), {@code iss} y {@code aud}. */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(JwtProperties propiedades, Clock reloj) {
        NimbusReactiveJwtDecoder decodificador = NimbusReactiveJwtDecoder
                .withSecretKey(new SecretKeySpec(propiedades.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator vigencia = new JwtTimestampValidator(Duration.ZERO);
        vigencia.setClock(reloj);
        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                expiracionObligatoria(), vigencia, new JwtIssuerValidator(propiedades.issuer()), audienciaEsperada()));
        return decodificador;
    }

    private static OAuth2TokenValidator<Jwt> expiracionObligatoria() {
        return jwt -> jwt.getExpiresAt() == null
                ? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "El token no tiene exp", null))
                : OAuth2TokenValidatorResult.success();
    }

    private static OAuth2TokenValidator<Jwt> audienciaEsperada() {
        return jwt -> jwt.getAudience() != null && jwt.getAudience().contains(JwtProperties.AUDIENCIA)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audiencia no válida", null));
    }

    @Bean
    JwtService jwtService(JwtProperties propiedades, Clock reloj) {
        return new JwtService(propiedades, reloj);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AutenticadorAdministrador autenticadorAdministrador(AdminProperties administrador, PasswordEncoder codificador) {
        return new AutenticadorAdministrador(administrador, codificador);
    }
}
