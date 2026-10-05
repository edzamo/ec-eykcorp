# ADR-014 · LocalStack 4.4.0 fijado, sin token

**Estado:** Aceptada

## Contexto

Se quiere simular AWS sin cuenta real. Desde marzo de 2026 la imagen `localstack/localstack:latest` exige un token de cuenta, y cualquier evaluador debe poder ejecutar el repositorio sin registrarse.

## Decisión

El Compose fija `localstack/localstack:4.4.0`, activable con el perfil `aws`, y limita `SERVICES` a `s3,sqs,secretsmanager`.

## Consecuencias

- Cualquiera puede clonar y ejecutar sin cuenta.
- La versión queda congelada y no recibe novedades salvo que se actualice a propósito.
- Si se migra a una versión con token, habrá que revisar este ADR.

## Alternativas descartadas

- `latest` con token: obliga a cada evaluador a registrarse.

[Volver al índice de ADR](README.md)
