# Decisiones de arquitectura

Resumen breve de las decisiones tomadas y de las excepciones deliberadas a las reglas.

## Decisiones

| Decisión | Motivo | Alternativa descartada |
|---|---|---|
| Arquitectura hexagonal ligera, un agregado | Cumple "por capas" y aísla el dominio sin sobreingeniería | CQRS o event sourcing: excesivo para un CRUD |
| WebFlux + R2DBC | Reactividad real; ningún hilo espera a la base de datos | JPA/JDBC: bloquearía el event loop |
| Flyway con JDBC solo al arrancar | R2DBC no migra esquemas | Scripts manuales |
| PostgreSQL para clientes, MongoDB para auditoría | La integridad y la unicidad del correo piden relacional; un historial de solo-añadir encaja en documentos | Todo en MongoDB (el enunciado pide PostgreSQL) |
| Vue 3 + Vite | El enunciado pide una SPA | Astro: orientado a contenido estático |
| Nginx como proxy inverso | Mismo origen, sin CORS | Exponer el backend |
| Borrado físico | El enunciado dice "eliminar" | Borrado lógico |
| JWT con administrador único | Seguridad básica sin gestión de usuarios | Tabla de usuarios y roles |
| Gradle (Kotlin DSL) con toolchain de Java 17 | Compila siempre con 17 y builds incrementales | Maven |
| Nombres `ms-<dominio>-<subdominio>` y SemVer | Estándar único que identifica dominio y propósito | Nombres genéricos |
| LocalStack 4.4.0 sin token | Cualquiera lo ejecuta sin registrarse (`latest` exige token) | `latest` con token |
| Lombok en infraestructura y servicios | Menos código repetitivo; el dominio sigue sin Lombok | Constructores escritos a mano |
| Contrato OpenAPI como fuente de verdad | API-first con pruebas de conformidad | Documentación generada desde el código |

## Excepciones deliberadas

| Regla relajada | Qué se hace y por qué | Estado |
|---|---|---|
| Sin librerías externas en `application` | Se permite `reactor-core`: los puertos devuelven `Mono`/`Flux` | Aplicada por defecto; pendiente de confirmar |
| Unidad de trabajo entre dos escrituras | Postgres y Mongo no comparten transacción; la auditoría es de mejor esfuerzo y su fallo se registra sin revertir el cambio | Aplicada por defecto; pendiente de confirmar |
| Todo caso de uso tiene adaptador de entrada con puerto | El login no usa puerto: es un mecanismo de seguridad de infraestructura | Aplicada por defecto; pendiente de confirmar |
| Sin Lombok ni SLF4J en `application` | Se permiten en los servicios; el dominio sigue puro y una regla ArchUnit lo exige | Autorizada por el titular del proyecto |
