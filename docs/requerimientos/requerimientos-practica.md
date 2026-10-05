# Requerimientos de la parte práctica

Resumen de lo que debe entregar la aplicación y dónde se cumple cada punto.

## Backend

Un microservicio con **Java 17, Spring Boot 3, PostgreSQL y Docker** que gestiona clientes (`id`, `nombres`, `apellidos`, `correo`, `teléfono`, `fecha_creación`) con estos endpoints: crear (`POST /clientes`), listar (`GET /clientes`), obtener por id (`GET /clientes/{id}`), actualizar (`PUT /clientes/{id}`) y eliminar (`DELETE /clientes/{id}`).

Requerimientos técnicos obligatorios: arquitectura por capas, DTOs, validaciones, manejo global de excepciones, logs básicos, variables de entorno, Dockerfile y Docker Compose funcionales.

## Frontend

Una SPA (Vue) que lista, crea, edita y elimina clientes, muestra mensajes de error y estados de carga, con componentes reutilizables, buenas prácticas, consumo correcto de la API REST y una organización de proyecto adecuada.

## Entregables

Código completo en un repositorio Git público, instrucciones de ejecución, README técnico (pasos de ejecución, estructura y consideraciones técnicas), Docker Compose funcional e historial de Git que refleje el proceso.

## Añadido por este proyecto

Programación reactiva de punta a punta (WebFlux y R2DBC), arquitectura hexagonal verificada por pruebas, autenticación JWT, auditoría de cambios en MongoDB, contrato OpenAPI como fuente de verdad con Swagger UI, pruebas unitarias, de integración y e2e, e integración continua.

## Trazabilidad

| Requisito | Dónde se cumple | Evidencia | Estado |
|---|---|---|---|
| CRUD de clientes (5 endpoints) | `ClienteController`, `ClienteService` | `ClienteE2ETest`, `scripts/smoke-test.sh` | Cumplido |
| Arquitectura por capas | Hexagonal: `domain`, `application`, `infrastructure` | `ArchitectureTest` (ArchUnit) | Cumplido |
| DTOs | `dto/request` y `dto/response` | `ClienteWebMapperTest`, `ConformidadContratoE2ETest` | Cumplido |
| Validaciones | Bean Validation en los DTO y reglas en el dominio | `ClienteTest`, `CorreoTest`, `TelefonoTest`, `ClienteControllerTest` | Cumplido |
| Manejo global de excepciones | `GlobalExceptionHandler` (ProblemDetail) | `GlobalExceptionHandlerTest` | Cumplido |
| Logs básicos | `@Slf4j` en servicios, controllers y adaptadores, sin datos personales | `ClienteServiceLoggingTest`, `AuthControllerLoggingTest` | Cumplido |
| Variables de entorno | `application.yml`, `.env.example`, `scripts/init-env.sh` | `docker compose config` | Cumplido |
| Dockerfile y Compose | `ms-*/Dockerfile`, `docker-compose.yml` | Job `imagenes-docker` del CI y la prueba de humo | Cumplido |
| Frontend: CRUD, errores y carga | `ms-cliente-presentacion/src` | Vitest con MSW (106 pruebas) | Cumplido |
| Componentes reutilizables | `ClienteForm` (alta y edición), `ConfirmDialog`, `ErrorAlert`, `LoadingSpinner` | Pruebas de componentes | Cumplido |
| README técnico e instrucciones | `README.md` y uno por microservicio | Revisión manual | Cumplido |
| Historial de Git | Ramas `feature/*`, `fix/*`, `refactor/*` fusionadas con `--no-ff` | `git log --graph` | Cumplido |
| AWS (stack de la empresa) | LocalStack con S3, SQS y Secrets Manager | `scripts/aws-local-deploy-frontend.sh` | **Parcial**: faltan los adaptadores del backend |
| HTTPS | Requisito de despliegue | n/a | **Pendiente** |
