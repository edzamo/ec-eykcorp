# ADR-010 · reactor-core permitido en application

**Estado:** Aceptada, pendiente de confirmación (EXC-1)

## Contexto

Los puertos devuelven `Mono` y `Flux`, de modo que la capa `application` necesita tipos de Reactor. Una hexagonal estricta lo prohibiría.

## Decisión

Se permite que `application` dependa de `reactor.core..` (y de `reactor.util.function`, `context` y `retry`), además de la anotación `@Service`. El dominio queda sin librerías externas. La regla de ArchUnit `la_aplicacion_no_depende_de_adaptadores_ni_frameworks` fija la lista blanca. Detalle en [EXC-1](../excepciones.md#exc-1--reactor-en-la-capa-de-aplicación).

## Consecuencias

- La reactividad llega hasta los casos de uso.
- Se relaja la pureza de `application`; los canarios impiden que se cuelen otras dependencias.
- Si se rechaza la excepción, hay que rediseñar los puertos.

## Alternativas descartadas

- Puertos síncronos con adaptadores que convierten: pierde el flujo reactivo.

[Volver al índice de ADR](README.md)
