# ADR-017 · Lombok limitado a infraestructura

**Estado:** Aceptada (hecha en `refactor/lombok-infraestructura`)

## Contexto

Lombok reduce código repetitivo, pero en el dominio y en la aplicación oculta la forma de las clases y las acopla a una librería. El dominio debe ser Java puro y `application` debe depender de lo mínimo.

## Decisión

Lombok se usa solo en `infrastructure` (por ejemplo `@RequiredArgsConstructor` en `ClienteController` y `@Slf4j` en `GlobalExceptionHandler`). Dominio y aplicación usan `record` y constructores explícitos. ArchUnit lo vigila: `el_dominio_es_java_puro` y `la_aplicacion_no_depende_de_adaptadores_ni_frameworks`, con canarios `lombok` en `ReglasArquitecturaTest`. `lombok.config` marca el código generado para que JaCoCo lo excluya.

## Consecuencias

- El núcleo queda libre de la librería y legible sin procesador de anotaciones.
- Se escribe a mano algo de código en `application`.
- La rama `feature/lombok-y-logs` propone revisar esta limitación ([ADR-019](adr-019-lombok-y-slf4j-en-application.md)).

## Alternativas descartadas

- Lombok en todo el código: acopla el núcleo.
- Sin Lombok en ningún sitio: más código repetitivo en infraestructura.

[Volver al índice de ADR](README.md)
