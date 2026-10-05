# ADR-001 · Hexagonal liviana con un solo agregado

**Estado:** Aceptada

## Contexto

El encargo exige una arquitectura por capas. El dominio del problema es pequeño: un único agregado (`Cliente`) y un CRUD. Hacía falta una estructura que separara la lógica de la tecnología y que se pudiera verificar, sin construir más de lo necesario.

## Decisión

Se adopta una arquitectura hexagonal ligera con tres paquetes (`domain`, `application`, `infrastructure`), un contexto delimitado y un agregado raíz. Los puertos viven en `application.port` y los adaptadores en `infrastructure.adapter`. La equivalencia con las capas clásicas (controller, service, repository, modelo, DTO) está en el [README principal, 5.3](../../../README.md#53-estructura-de-paquetes).

## Consecuencias

- Cumple el requisito de capas y aísla el dominio de Spring, R2DBC y HTTP.
- La regla de dependencias se puede comprobar con ArchUnit (`ArchitectureTest`).
- Hay más ficheros y más traducciones (mappers) que en un CRUD plano.

## Alternativas descartadas

- CQRS y event sourcing: excesivos para un CRUD de una entidad.
- Capas clásicas sin puertos: más simples, pero el servicio quedaría acoplado a la persistencia.

[Volver al índice de ADR](README.md)
