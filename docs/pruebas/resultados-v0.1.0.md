# Resultados de verificación de la v0.1.0

Estado verificado de la rama `main` en la versión 0.1.0. Las cifras provienen de la ejecución completa de las suites y de la prueba de humo sobre el sistema levantado con Docker Compose. Cómo reproducirlas: [estrategia.md](estrategia.md#4-cómo-ejecutarlas).

## Resumen

| Componente | Pruebas | Herramientas | Cobertura de líneas |
|---|---|---|---|
| Backend `ms-cliente-gestion` | **217** | JUnit 5, ArchUnit, Testcontainers con PostgreSQL y MongoDB reales | **95 %** |
| Frontend `ms-cliente-presentacion` | **106** | Vitest, Vue Test Utils y MSW | **100 %** |
| Sistema completo | **14 comprobaciones** | `scripts/smoke-test.sh` a través de Nginx | No aplica |

El backend exige como mínimo 80 % de líneas y 70 % de ramas (`jacocoTestCoverageVerification` en `ms-cliente-gestion/build.gradle.kts`); el 95 % de líneas supera el umbral. El CI tiene tres jobs (`ms-cliente-gestion`, `ms-cliente-presentacion` e `imagenes-docker`).

## Prueba de humo

`scripts/smoke-test.sh` se ejecuta contra `http://localhost:8080` (Nginx) con el stack levantado. Requiere `ADMIN_USER` y `ADMIN_PASSWORD`. Las 14 comprobaciones, en orden:

| # | Grupo | Comprobación | Esperado |
|---|---|---|---|
| 1 | Frontend y proxy | `GET /` (SPA) | 200 |
| 2 | Frontend y proxy | `GET /api/actuator/health` | 200 |
| 3 | Seguridad | `GET /api/clientes` sin token | 401 |
| 4 | Seguridad | Login con contraseña incorrecta | 401 |
| 5 | Seguridad | Login correcto (obtiene el token) | 200 |
| 6 | CRUD | `POST /clientes` con teléfono | 201 |
| 7 | CRUD | `POST /clientes` sin teléfono | 201 |
| 8 | CRUD | `POST /clientes` con correo duplicado | 409 |
| 9 | CRUD | `POST /clientes` con datos inválidos | 400 |
| 10 | CRUD | `GET /clientes` | 200 |
| 11 | CRUD | `GET /clientes/{id}` | 200 |
| 12 | CRUD | `PUT /clientes/{id}` | 200 |
| 13 | CRUD | `DELETE /clientes/{id}` | 204 |
| 14 | CRUD | `GET /clientes/{id}` ya borrado | 404 |

Si alguna falla, el script muestra el cuerpo de la respuesta, indica cuántas fallaron y sale con código distinto de cero.

## Qué cubre cada resultado

- **Dominio y casos de uso:** invariantes, correo duplicado, cliente inexistente, fecha de creación inmutable y auditoría (`ClienteTest`, `CorreoTest`, `TelefonoTest`, `ClienteServiceTest`).
- **Contratos de repositorio y auditoría:** el mismo contrato pasa contra el doble en memoria y contra PostgreSQL y MongoDB reales.
- **API y errores:** 201, 400, 404, 409 y 500 sin filtrar detalles; 401 y 403 con `ProblemDetail`.
- **Seguridad:** firma, expiración, emisor y audiencia del token; login sin enumeración de usuarios.
- **Arquitectura:** las 8 reglas de `ArchitectureTest` y los canarios de `ReglasArquitecturaTest`.
- **Resiliencia:** MongoDB caído no impide crear clientes ni marca el servicio como `DOWN`.

## Deuda de pruebas

Lo que **no** está cubierto en la v0.1.0:

| Tema | Detalle |
|---|---|
| Pruebas de navegador (Playwright) | No existen. La interfaz se cubre con Vitest y MSW; el sistema completo, con el script de humo por API. No se verifica la interacción real en un navegador |
| Pruebas de carga y rendimiento | No existen. No hay medición de latencia ni de capacidad bajo concurrencia, más allá de la prueba de altas simultáneas con el mismo correo |
| Análisis de vulnerabilidades de Gradle en CI | No automático. Se revisó a mano con OSV; Dependabot está activo. El frontend sí ejecuta `npm audit` en CI |
| SonarQube | No hay análisis estático centralizado |
| Prueba de humo en CI | El script se ejecuta a mano; el CI solo construye las imágenes |
| Épica 4 | Sin pruebas de los adaptadores de Secrets Manager y SQS (aún no existen). Tampoco hay pruebas automáticas del aprovisionamiento de LocalStack ni del despliegue de la SPA a S3 simulado: se verificaron a mano |
| Cabeceras de Nginx y límite de login | Verificados por revisión; sin prueba automática |
| HTTPS | No implementado, por tanto sin pruebas |
| Límite de intentos de login en la aplicación | No existe (sí en Nginx) |
| Contrato OpenAPI | En curso en la rama `feature/openapi-contract-first`; todavía no hay pruebas de contrato en `main` |
