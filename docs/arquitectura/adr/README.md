# Registro de decisiones de arquitectura (ADR)

Cada decisión relevante se documenta en un archivo con el formato **Estado, Contexto, Decisión, Consecuencias y Alternativas descartadas**. La tabla resumida original está en el [README principal, sección 15](../../../README.md#15-decisiones-de-arquitectura-adr). Las decisiones 001 a 016 son las de esa tabla; la 017 recoge un refactor ya hecho, y la 018 y la 019 están **en curso** y no forman parte de `main`.

| # | Decisión | Estado |
|---|---|---|
| 001 | [Hexagonal liviana con un solo agregado](adr-001-hexagonal-liviana.md) | Aceptada |
| 002 | [WebFlux y R2DBC para reactividad de punta a punta](adr-002-webflux-r2dbc.md) | Aceptada |
| 003 | [Flyway con JDBC solo al arrancar](adr-003-flyway-jdbc-solo-al-arrancar.md) | Aceptada |
| 004 | [Vue 3 con Vite para la SPA](adr-004-vue-3-vite.md) | Aceptada |
| 005 | [Nginx como servidor de la SPA y reverse proxy](adr-005-nginx-reverse-proxy.md) | Aceptada |
| 006 | [Borrado físico de clientes](adr-006-borrado-fisico.md) | Aceptada |
| 007 | [JWT con un único administrador por entorno](adr-007-jwt-usuario-unico.md) | Aceptada (login sin caso de uso pendiente de confirmación) |
| 008 | [MongoDB solo para la auditoría](adr-008-mongodb-solo-auditoria.md) | Aceptada |
| 009 | [Español para dominio y documentación, inglés para infraestructura](adr-009-idioma-espanol-dominio.md) | Aceptada |
| 010 | [reactor-core permitido en application](adr-010-reactor-core-en-application.md) | Aceptada, pendiente de confirmación (EXC-1) |
| 011 | [Auditoría de mejor esfuerzo](adr-011-auditoria-mejor-esfuerzo.md) | Aceptada, pendiente de confirmación (EXC-2) |
| 012 | [Adaptador en memoria solo para pruebas](adr-012-adaptador-en-memoria-para-tests.md) | Aceptada |
| 013 | [Gradle (Kotlin DSL) con toolchain de Java 17](adr-013-gradle-toolchain-java-17.md) | Aceptada |
| 014 | [LocalStack 4.4.0 fijado, sin token](adr-014-localstack-sin-token.md) | Aceptada |
| 015 | [Servicios AWS acotados a Secrets Manager, SQS y S3](adr-015-servicios-aws-acotados.md) | Aceptada (implementación parcial, Épica 4) |
| 016 | [Nombres ms-dominio-subdominio y versionado SemVer por componente](adr-016-nombres-de-microservicios-y-semver.md) | Aceptada |
| 017 | [Lombok limitado a infraestructura](adr-017-lombok-limitado-a-infraestructura.md) | Aceptada (hecha en `refactor/lombok-infraestructura`) |
| 018 | [Contrato OpenAPI como fuente de verdad](adr-018-contrato-openapi-fuente-de-verdad.md) | En curso |
| 019 | [Lombok y SLF4J en application (EXC-3)](adr-019-lombok-y-slf4j-en-application.md) | Aceptada (rama `feature/lombok-y-logs`, pendiente de fusionar) |

Excepciones a las reglas de arquitectura (EXC-1, EXC-2 y login sin caso de uso): [`../excepciones.md`](../excepciones.md).
