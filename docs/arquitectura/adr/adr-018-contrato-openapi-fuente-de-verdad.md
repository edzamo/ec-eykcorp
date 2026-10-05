# ADR-018 · Contrato OpenAPI como fuente de verdad

**Estado:** En curso

## Contexto

Hoy el contrato REST está descrito a mano en el [README principal, 8.2](../../../README.md#82-contrato-rest) y garantizado por las pruebas. Un contrato máquina-legible evitaría que documentación, backend y cliente diverjan.

## Decisión

Se explora un enfoque *contract-first*: el contrato OpenAPI se escribe primero y es la referencia del backend y de las pruebas. El trabajo está en la rama `feature/openapi-contract-first` y **todavía no forma parte de `main`**. La decisión definitiva y su alcance se fijarán al cerrar esa rama.

## Consecuencias

- Todavía no hay consecuencias en `main`: la fuente de verdad sigue siendo el README y las pruebas.
- Se prevé reducir el riesgo de divergencia entre contrato y código.

## Alternativas descartadas

- Generar el contrato a partir del código (*code-first*): se evaluará al cerrar la rama.

[Volver al índice de ADR](README.md)
