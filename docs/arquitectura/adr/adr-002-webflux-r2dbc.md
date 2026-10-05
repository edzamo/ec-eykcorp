# ADR-002 · WebFlux y R2DBC para reactividad de punta a punta

**Estado:** Aceptada

## Contexto

Un backend reactivo solo lo es si todas las capas lo son: un acceso JDBC bloquearía los hilos del event loop de WebFlux. El proyecto quiere mostrar programación reactiva real, no una fachada.

## Decisión

El backend usa `spring-boot-starter-webflux` y `spring-boot-starter-data-r2dbc` con `r2dbc-postgresql`. Los puertos devuelven `Mono` y `Flux`. Una regla de ArchUnit (`sin_llamadas_bloqueantes_de_reactor`) prohíbe `Mono.block*`, `Flux.blockFirst`, `blockLast`, `toIterable` y `toStream` en producción.

## Consecuencias

- Ningún hilo del event loop espera a la base de datos.
- Las pruebas usan `StepVerifier` y `WebTestClient`.
- La curva de aprendizaje es mayor y la depuración de flujos es menos directa que con código bloqueante.
- Obliga a una excepción de arquitectura: [EXC-1](../excepciones.md).

## Alternativas descartadas

- JPA/JDBC con Spring MVC: bloquearía el event loop y rompería la reactividad.

[Volver al índice de ADR](README.md)
