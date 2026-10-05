# Requerimientos de la parte práctica

Este documento recoge, con palabras propias, lo que se pidió para la parte práctica de la prueba técnica de desarrollador fullstack, añade los requisitos que el proyecto se impuso por su cuenta y cierra con una **matriz de trazabilidad** que enlaza cada requisito con el lugar donde se cumple y con la evidencia que lo demuestra.

Estado de referencia: versión **v0.1.0** (rama `main`). Ver también el [README principal, sección 2](../../README.md#2-requerimientos).

## 1. Requerimientos del encargo

### 1.1 Backend

Un microservicio con **Java 17, Spring Boot 3, PostgreSQL y Docker** que gestione clientes.

**Datos de un cliente:** identificador, nombres, apellidos, correo electrónico, teléfono y fecha de creación del registro.

**Operaciones expuestas por API REST:**

| Operación | Método y ruta |
|---|---|
| Crear un cliente | `POST /clientes` |
| Listar clientes | `GET /clientes` |
| Consultar un cliente por id | `GET /clientes/{id}` |
| Modificar un cliente | `PUT /clientes/{id}` |
| Eliminar un cliente | `DELETE /clientes/{id}` |

**Condiciones técnicas obligatorias:**

- Arquitectura organizada en capas.
- Uso correcto de DTOs (no exponer el modelo interno).
- Validación de los datos de entrada.
- Manejo global de excepciones.
- Registro de eventos (logs) básico.
- Configuración mediante variables de entorno.
- `Dockerfile` funcional.
- Docker Compose funcional.

### 1.2 Frontend

Una SPA en Vue o Astro (este proyecto usa **Vue 3**) que permita:

- Listar, crear, editar y eliminar clientes.
- Mostrar mensajes de error.
- Mostrar estados de carga.

Con estas condiciones técnicas: componentes reutilizables, buenas prácticas, consumo correcto de la API REST y una organización de proyecto adecuada.

### 1.3 Entregables

- Código fuente completo en un repositorio Git público.
- Instrucciones de ejecución.
- README técnico: pasos de ejecución, estructura del proyecto y consideraciones técnicas.
- Docker Compose funcional.
- Historial de Git que refleje cómo se fue construyendo la solución.

## 2. Requerimientos añadidos por el proyecto

Decisiones propias que van más allá del encargo:

| Id | Requisito añadido | Referencia |
|---|---|---|
| RA-01 | Programación reactiva de punta a punta (WebFlux y R2DBC), sin llamadas bloqueantes | [ADR-002](../arquitectura/adr/adr-002-webflux-r2dbc.md) |
| RA-02 | Arquitectura hexagonal con la regla de dependencias verificada automáticamente | [ADR-001](../arquitectura/adr/adr-001-hexagonal-liviana.md), [ADR-010](../arquitectura/adr/adr-010-reactor-core-en-application.md) |
| RA-03 | API protegida con JWT (usuario administrador único, por variables de entorno) | [ADR-007](../arquitectura/adr/adr-007-jwt-usuario-unico.md) |
| RA-04 | Auditoría de cada cambio sobre un cliente en MongoDB, de mejor esfuerzo | [ADR-008](../arquitectura/adr/adr-008-mongodb-solo-auditoria.md), [ADR-011](../arquitectura/adr/adr-011-auditoria-mejor-esfuerzo.md) |
| RA-05 | Pruebas unitarias, de integración y de extremo a extremo en backend y frontend | [Estrategia de pruebas](../pruebas/estrategia.md) |
| RA-06 | Integración continua con GitHub Actions | `.github/workflows/ci.yml` |
| RA-07 | Simulación local de AWS con LocalStack (Secrets Manager, SQS, S3) | [ADR-014](../arquitectura/adr/adr-014-localstack-sin-token.md), [ADR-015](../arquitectura/adr/adr-015-servicios-aws-acotados.md). **Parcial** |
| RA-08 | Diagramas UML versionados como código (Mermaid) | [README principal, secciones 4 a 6](../../README.md#4-vista-general-de-la-arquitectura) |
| RA-09 | Contrato OpenAPI como fuente de verdad de la API | [ADR-018](../arquitectura/adr/adr-018-contrato-openapi-fuente-de-verdad.md). **En curso** en la rama `feature/openapi-contract-first`; todavía no forma parte de `main` |

## 3. Matriz de trazabilidad

Leyenda de estado: **Cumplido**, **Parcial**, **Pendiente**. Las rutas son relativas a la raíz del repositorio. `GESTION` abrevia `ms-cliente-gestion/src/main/java/com/eykcorp/clientes` y `GESTION-TEST` abrevia `ms-cliente-gestion/src/test/java/com/eykcorp/clientes`.

### 3.1 Backend

| Requisito | Dónde se cumple | Evidencia | Estado |
|---|---|---|---|
| Java 17, Spring Boot 3 | `ms-cliente-gestion/build.gradle.kts` (toolchain Java 17, Spring Boot 3.5.16) | `./gradlew check` en el job `ms-cliente-gestion` de CI | Cumplido |
| PostgreSQL | `GESTION/infrastructure/adapter/out/persistence/ClientePersistenceAdapter`, migración `db/migration/V1__crear_tabla_clientes.sql` | `GESTION-TEST/infrastructure/adapter/out/persistence/ClientePersistenceAdapterIT` (PostgreSQL real con Testcontainers) | Cumplido |
| Datos del cliente (id, nombres, apellidos, correo, teléfono, fecha de creación) | `GESTION/domain/cliente/Cliente`, `Correo`, `Telefono`; tabla `clientes` | `GESTION-TEST/domain/cliente/ClienteTest`, `CorreoTest`, `TelefonoTest` | Cumplido |
| `POST /clientes` | `GESTION/infrastructure/adapter/in/web/ClienteController` | `ClienteControllerTest`, `ClienteE2ETest`, `scripts/smoke-test.sh` | Cumplido |
| `GET /clientes` | `ClienteController` | `ClienteControllerTest`, `scripts/smoke-test.sh` | Cumplido |
| `GET /clientes/{id}` | `ClienteController` | `ClienteControllerTest`, `scripts/smoke-test.sh` | Cumplido |
| `PUT /clientes/{id}` | `ClienteController` | `ClienteControllerTest`, `ClienteE2ETest`, `scripts/smoke-test.sh` | Cumplido |
| `DELETE /clientes/{id}` | `ClienteController` | `ClienteControllerTest`, `ClienteE2ETest`, `scripts/smoke-test.sh` | Cumplido |
| Arquitectura por capas | Paquetes `domain`, `application`, `infrastructure` (equivalencias en el [README principal, 5.3](../../README.md#53-estructura-de-paquetes)) | `GESTION-TEST/ArchitectureTest` (8 reglas) y `ReglasArquitecturaTest` (canarios) | Cumplido |
| DTOs | `ClienteRequest`, `ClienteResponse`, `ClienteWebMapper` en `GESTION/infrastructure/adapter/in/web` | `ClienteWebMapperTest`, `ClienteControllerTest` | Cumplido |
| Validaciones | `ClienteRequest` (Bean Validation) y las invariantes de `Cliente`, `Correo`, `Telefono` | `ClienteControllerTest` (400 con errores por campo), `ClienteTest`, `CorreoTest`, `TelefonoTest` | Cumplido |
| Manejo global de excepciones | `GESTION/infrastructure/adapter/in/web/GlobalExceptionHandler` (RFC 7807 `ProblemDetail`), `GESTION/infrastructure/security/ProblemaSeguridadHandler` para 401 y 403 | `GlobalExceptionHandlerTest`, `ClienteControllerTest`, `SeguridadWebTest` | Cumplido |
| Logs básicos | `GlobalExceptionHandler` (error inesperado a nivel `ERROR`), `ClienteService` (fallos de auditoría a nivel `WARNING`, sin datos personales); nivel por `LOG_LEVEL` | `ClienteServiceLoggingTest`, `ClienteControllerTest` (500 sin filtrar detalles) | Cumplido, con matiz: los casos de uso no registran cada operación; ver [ADR-019](../arquitectura/adr/adr-019-lombok-y-slf4j-en-application.md) (propuesta en curso) |
| Variables de entorno | `ms-cliente-gestion/src/main/resources/application.yml`, `.env.example`, `docker-compose.yml` (variables obligatorias con `:?`) | `docker compose config` y arranque del stack en `scripts/smoke-test.sh`; `JwtPropertiesTest` | Cumplido |
| Dockerfile funcional | `ms-cliente-gestion/Dockerfile` (multi-stage JDK 17 a JRE 17, usuario sin privilegios) | Job `imagenes-docker` de CI; `scripts/smoke-test.sh` | Cumplido |
| Docker Compose funcional | `docker-compose.yml` (postgres, mongo, ms-cliente-gestion, ms-cliente-presentacion; perfil `aws` opcional) | Job `imagenes-docker` de CI; `scripts/smoke-test.sh` (14 comprobaciones) | Cumplido |

### 3.2 Frontend

| Requisito | Dónde se cumple | Evidencia | Estado |
|---|---|---|---|
| SPA con Vue 3 | `ms-cliente-presentacion/src` (Vite, Composition API) | `npm run build` en el job `ms-cliente-presentacion` de CI | Cumplido |
| Listar clientes | `src/pages/ClientesPage.vue`, `src/components/ClienteTable.vue`, `src/domain/useClientes.js` | `tests/unit/pages/ClientesPage.test.js`, `tests/unit/components/ClienteTable.test.js`, `tests/unit/domain/useClientes.test.js` | Cumplido |
| Crear y editar clientes | `src/components/ClienteForm.vue` | `tests/unit/components/ClienteForm.test.js`, `ClientesPage.test.js` | Cumplido |
| Eliminar clientes (con confirmación) | `src/components/ConfirmDialog.vue` | `tests/unit/components/ConfirmDialog.test.js`, `ClientesPage.test.js` | Cumplido |
| Mensajes de error | `src/components/ErrorAlert.vue`; `ApiError` en `src/services/httpClient.js` | `tests/unit/components/ErrorAlert.test.js`, `tests/unit/services/httpClient.test.js` | Cumplido |
| Estados de carga | `src/components/LoadingSpinner.vue`; estado `cargando` en `useClientes` | `tests/unit/components/LoadingSpinner.test.js`, `tests/unit/domain/useClientes.test.js` | Cumplido |
| Componentes reutilizables | `src/components/` (cinco componentes presentacionales, sin llamadas HTTP) | Pruebas de cada componente en `tests/unit/components/` | Cumplido |
| Consumo correcto de la API REST | `src/services/clienteService.js` sobre un único cliente HTTP `src/services/httpClient.js` | `tests/unit/services/clienteService.test.js`, `tests/unit/app/App.integration.test.js` (MSW) | Cumplido |
| Organización del proyecto | `src/app`, `src/domain`, `src/services`, `src/components`, `src/pages`; reglas en el [README principal, sección 6](../../README.md#6-frontend-arquitectura-vue-3) | `npm run lint` y `npm run format:check` en CI | Cumplido |

### 3.3 Entregables

| Requisito | Dónde se cumple | Evidencia | Estado |
|---|---|---|---|
| Código en repositorio Git público | Repositorio remoto del proyecto | Verificable al clonar | Cumplido |
| Instrucciones de ejecución | [README principal, sección 14](../../README.md#14-cómo-ejecutar) y README de cada microservicio | Seguir los pasos sobre un clon limpio | Cumplido |
| README técnico | `README.md`, `ms-cliente-gestion/README.md`, `ms-cliente-presentacion/README.md` | Lectura | Cumplido |
| Docker Compose funcional | `docker-compose.yml` | `scripts/smoke-test.sh` contra el stack levantado | Cumplido |
| Historial de Git que refleje el proceso | Ramas `feature/*`, `fix/*`, `refactor/*` y `docs/*` fusionadas con `--no-ff` ([README principal, 13.2](../../README.md#132-estrategia-de-ramas-y-commits)) | `git log --graph` | Cumplido, con matiz: las primeras entregas entraron a `main` sin fusión explícita (documentado en el README) |

### 3.4 Requisitos añadidos por el proyecto

| Requisito | Dónde se cumple | Evidencia | Estado |
|---|---|---|---|
| RA-01 Reactivo de punta a punta | `spring-boot-starter-webflux`, `r2dbc-postgresql`, Mongo reactivo; devolución de `Mono` y `Flux` en puertos | Regla `sin_llamadas_bloqueantes_de_reactor` en `ArchitectureTest` y su canario en `ReglasArquitecturaTest` | Cumplido |
| RA-02 Hexagonal verificada | Puertos en `GESTION/application/port`, adaptadores en `GESTION/infrastructure/adapter` | `ArchitectureTest`, `ReglasArquitecturaTest` | Cumplido, con excepciones EXC-1 y EXC-2 pendientes de confirmación |
| RA-03 JWT | `GESTION/infrastructure/security/SecurityConfig`, `JwtService`, `AutenticadorAdministrador`; `GESTION/infrastructure/adapter/in/web/AuthController`; en frontend `src/domain/useAuth.js`, `src/pages/LoginPage.vue`, guard en `src/app/router.js` | `SeguridadWebTest`, `JwtServiceTest`, `AutenticadorAdministradorTest`, `ClienteE2ETest`; `tests/unit/app/router.test.js`, `tests/unit/domain/useAuth.test.js` | Cumplido (login sin caso de uso, pendiente de confirmación) |
| RA-04 Auditoría en MongoDB | `GESTION/application/port/out/AuditoriaPort`, `GESTION/infrastructure/adapter/out/audit/AuditoriaMongoPublisher` | `AuditoriaPortContract` (contra fake y contra MongoDB real), `AuditoriaMongoPublisherIT`, `ClienteMongoCaidoE2ETest` | Cumplido |
| RA-05 Pruebas en los tres niveles | Backend y frontend | 217 tests de backend y 106 de frontend ([resultados](../pruebas/resultados-v0.1.0.md)) | Cumplido (sin pruebas de navegador, ver deuda) |
| RA-06 CI con GitHub Actions | `.github/workflows/ci.yml` (tres jobs: `ms-cliente-gestion`, `ms-cliente-presentacion`, `imagenes-docker`) | Ejecuciones del workflow | Cumplido |
| RA-07 AWS local con LocalStack | `docker-compose.yml` (servicio `localstack`, perfil `aws`), `infra/localstack/init/ready.d/01-recursos.sh`, `scripts/aws-local-deploy-frontend.sh` | Ejecución manual descrita en el README principal, sección 10.5 | **Parcial**: faltan el adaptador de Secrets Manager y el de SQS en el backend |
| RA-08 Diagramas como código | Bloques Mermaid del README principal | Renderizado en el repositorio | Cumplido |
| RA-09 Contrato OpenAPI | Rama `feature/openapi-contract-first` | Aún sin evidencia en `main` | **En curso** |

### 3.5 Pendientes y deuda declarada

Estos puntos no son requisitos del encargo, pero un evaluador debe conocerlos:

| Tema | Estado | Detalle |
|---|---|---|
| HTTPS y cabecera HSTS | Pendiente | La versión sirve HTTP en el puerto 8080; el TLS se termina delante (balanceador o proxy) o se añade a Nginx con un certificado. Ver [README principal, sección 9](../../README.md#9-seguridad-jwt) |
| Pruebas de navegador (Playwright) | Pendiente | La interfaz se cubre con Vitest y MSW y el sistema completo con el script de humo por API |
| SonarQube | Pendiente | No hay análisis estático centralizado |
| Análisis de vulnerabilidades de Gradle en CI | Pendiente | Se hizo a mano con OSV; Dependabot está activo. El frontend sí ejecuta `npm audit` en CI |
| Límite de intentos de login en la aplicación | Pendiente | Sí existe a nivel de Nginx (5 por minuto por IP) |
| Épica 4 (adaptadores de Secrets Manager y SQS) | Parcial | Ver [historia E4](../historias/e4-aws-local.md) |
