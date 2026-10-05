package com.eykcorp.clientes.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.test.StepVerifier;

class JwtServiceTest {

    private static final Instant AHORA = TokensDePrueba.AHORA;
    private static final String EMISOR = "ms-cliente-crud";

    private final JwtService servicio = TokensDePrueba.servicio(TokensDePrueba.SECRETO, EMISOR, AHORA);

    private static ReactiveJwtDecoder decodificadorEn(Instant instante) {
        return new SecurityConfig().jwtDecoder(
                TokensDePrueba.propiedades(TokensDePrueba.SECRETO, EMISOR), Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void debe_emitir_un_token_valido_con_sub_iss_iat_y_exp() {
        TokenEmitido emitido = servicio.emitir("admin");

        StepVerifier.create(decodificadorEn(AHORA).decode(emitido.token()))
                .assertNext((Jwt jwt) -> {
                    assertThat(jwt.getSubject()).isEqualTo("admin");
                    assertThat(jwt.getClaimAsString("iss")).isEqualTo(EMISOR);
                    assertThat(jwt.getIssuedAt()).isEqualTo(AHORA);
                    assertThat(jwt.getExpiresAt()).isEqualTo(AHORA.plusSeconds(30 * 60));
                    assertThat(jwt.getHeaders()).containsEntry("alg", "HS256");
                })
                .verifyComplete();
        assertThat(emitido.expiraEnSegundos()).isEqualTo(1800);
    }

    @Test
    void debe_rechazar_un_token_expirado_segun_el_reloj() {
        String token = servicio.emitir("admin").token();

        StepVerifier.create(decodificadorEn(AHORA.plusSeconds(30 * 60 + 1)).decode(token))
                .expectError().verify();
    }

    @Test
    void debe_rechazar_un_token_firmado_con_otra_clave() {
        String token = TokensDePrueba.servicio(TokensDePrueba.OTRO_SECRETO, EMISOR, AHORA).emitir("admin").token();

        StepVerifier.create(decodificadorEn(AHORA).decode(token)).expectError().verify();
    }

    @Test
    void debe_rechazar_un_token_con_issuer_incorrecto() {
        String token = TokensDePrueba.servicio(TokensDePrueba.SECRETO, "otro-emisor", AHORA).emitir("admin").token();

        StepVerifier.create(decodificadorEn(AHORA).decode(token)).expectError().verify();
    }

    @Test
    void debe_rechazar_un_token_sin_expiracion() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject("admin").issuer(EMISOR)
                .issueTime(Date.from(AHORA)).build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(TokensDePrueba.SECRETO.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        StepVerifier.create(decodificadorEn(AHORA).decode(jwt.serialize())).expectError().verify();
    }

    @Test
    void debe_rechazar_un_token_sin_firma_alg_none() {
        String sinFirma = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"none\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8))
                + "." + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(
                        ("{\"sub\":\"admin\",\"iss\":\"" + EMISOR + "\",\"exp\":4102444800}")
                                .getBytes(java.nio.charset.StandardCharsets.UTF_8)) + ".";

        StepVerifier.create(decodificadorEn(AHORA).decode(sinFirma)).expectError().verify();
    }
}
