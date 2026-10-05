# ADR-012 · Adaptador en memoria solo para pruebas

**Estado:** Aceptada

## Contexto

Las pruebas de los casos de uso deben ser rápidas y no depender de contenedores. Al mismo tiempo, el doble de prueba debe comportarse como el adaptador real.

## Decisión

`InMemoryClienteRepository` y `FakeAuditoriaPort` (en `src/test`) implementan los puertos con estructuras en memoria. Un contrato compartido (`ClienteRepositoryPortContract`, `AuditoriaPortContract`) se ejecuta contra el fake y contra el adaptador real con Testcontainers, de modo que ambos se mantienen equivalentes.

## Consecuencias

- `ClienteServiceTest` corre en milisegundos y sin infraestructura.
- Si el fake y el adaptador divergen, el contrato lo detecta.
- Más clases de prueba que mantener.

## Alternativas descartadas

- H2 con R2DBC: no aporta frente a Testcontainers con PostgreSQL real.

[Volver al índice de ADR](README.md)
