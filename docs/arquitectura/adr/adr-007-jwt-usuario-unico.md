# ADR-007 · JWT con un único administrador por entorno

**Estado:** Aceptada (login sin caso de uso pendiente de confirmación)

## Contexto

Se quiere proteger la API sin construir gestión de usuarios, que está fuera del alcance (registro, roles, refresh tokens).

## Decisión

`POST /auth/login` valida el usuario y la contraseña de un administrador definido por variables de entorno (`ADMIN_USER`, `ADMIN_PASSWORD_HASH` con BCrypt) y emite un JWT HS256 con `exp`, `iss` y `aud`. Spring Security WebFlux valida el token en `/clientes/**`. El login se implementa en infraestructura (`AutenticadorAdministrador`, `JwtService`) sin caso de uso, lo que está recogido como [excepción pendiente de confirmación](../excepciones.md#login-sin-caso-de-uso).

## Consecuencias

- API protegida sin base de datos de usuarios.
- Sin refresh tokens ni roles: si el alcance crece, hay que promover el login a un puerto `CredencialesPort`.
- El secreto debe tener al menos 32 caracteres; sin él la aplicación no arranca (`JwtPropertiesTest`).
- No hay límite de intentos de login en la aplicación; solo el de Nginx.

## Alternativas descartadas

- Tabla de usuarios y roles: fuera de alcance.

[Volver al índice de ADR](README.md)
