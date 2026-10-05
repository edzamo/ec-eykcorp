package com.eykcorp.clientes.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;

/** Emite JWT HS256 (sub, iss, aud, iat, exp) usando el {@link Clock} inyectado. */
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties propiedades;
    private final Clock reloj;

    public TokenEmitido emitir(String usuario) {
        Instant ahora = reloj.instant();
        Duration vigencia = Duration.ofMinutes(propiedades.expirationMinutes());
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(usuario)
                .issuer(propiedades.issuer())
                .audience(JwtProperties.AUDIENCIA)
                .issueTime(Date.from(ahora))
                .expirationTime(Date.from(ahora.plus(vigencia)))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(propiedades.secret().getBytes(StandardCharsets.UTF_8)));
            return new TokenEmitido(jwt.serialize(), vigencia.toSeconds());
        } catch (JOSEException e) {
            throw new IllegalStateException("No se pudo firmar el token", e);
        }
    }
}
