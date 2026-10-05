# ADR-019 · Lombok y SLF4J en application (EXC-3)

**Estado:** Aceptada. Implementada en la rama `feature/lombok-y-logs`, pendiente de fusionar a `main`

## Contexto

Hoy `application` no puede usar Lombok ni SLF4J: `ClienteService` registra con `System.Logger` y el puente hacia SLF4J se comprueba en `ClienteServiceLoggingTest`. Esto obliga a escribir más código y deja los registros de los casos de uso limitados a los fallos de auditoría.

## Decisión

La rama `feature/lombok-y-logs` permite Lombok y SLF4J en `application` y registrar los casos de uso de forma más completa. Supondría una nueva excepción (EXC-3) a las reglas de ArchUnit. **No está aceptada ni integrada en `main`.**

## Consecuencias

- Si se aprueba: menos código repetitivo y registros más ricos en los casos de uso, a costa de acoplar `application` a dos librerías y de relajar las reglas y sus canarios.
- Si se rechaza: se mantiene el estado actual ([ADR-017](adr-017-lombok-limitado-a-infraestructura.md)).

## Alternativas descartadas

- Mantener `System.Logger` y la regla estricta (estado actual).

[Volver al índice de ADR](README.md)
