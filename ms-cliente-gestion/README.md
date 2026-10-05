# ms-cliente-gestion

Microservicio de **gestión de clientes**: crea, consulta, actualiza y elimina clientes, protege la API con JWT y registra una auditoría de cada cambio. Es el backend del sistema; la interfaz web es [`ms-cliente-presentacion`](../ms-cliente-presentacion/README.md).

| | |
|---|---|
| Versión | `0.1.0` (SemVer; la misma versión etiqueta la imagen `eykcorp/ms-cliente-gestion:0.1.0`) |
| Lenguaje y framework | Java 17, Spring Boot 3.5.6, **Spring WebFlux** (programación reactiva) |
| Datos | PostgreSQL 16 (clientes, vía R2DBC + Flyway) y MongoDB 7 (auditoría) |
| Seguridad | Spring Security + JWT HS256 stateless |
| Build | Gradle 9.2 (Kotlin DSL, wrapper incluido, toolchain Java 17) |

## 1. Qué necesitas para levantarlo

| Requisito | Para qué | Versión |
|---|---|---|
| Docker y Docker Compose v2 | Levantar todo el sistema, y los tests de integración (Testcontainers) | Docker 20.10+ |
| JDK 17 | Solo si lo ejecutas fuera de Docker | 17 (Gradle lo descarga como *toolchain* si no está) |
| Nada más | El wrapper `./gradlew` descarga Gradle | — |

## 2. Cómo levantarlo

### Opción A. Todo el sistema con Docker Compose (recomendada)

Desde la **raíz del repositorio**:

```bash
./scripts/init-env.sh       # genera .env: secretos aleatorios y usuario de demostración (admin / Eyk-Demo-2026)
docker compose up --build
```

La aplicación queda en **http://localhost:8080** (Nginx sirve la web y reenvía `/api/*` a este servicio). El backend no publica puertos al host.

### Opción B. Backend en tu máquina (desarrollo)

```bash
# 1) Solo las bases de datos, publicadas en 127.0.0.1
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d postgres mongo

# 2) Variables de entorno (ver tabla de abajo); reutiliza las de tu .env
set -a; source .env; set +a
export SPRING_R2DBC_URL=r2dbc:postgresql://localhost:5432/$POSTGRES_DB
export SPRING_FLYWAY_URL=jdbc:postgresql://localhost:5432/$POSTGRES_DB
export SPRING_DATA_MONGODB_URI="mongodb://$MONGO_USER:$MONGO_PASSWORD@localhost:27017/auditoria?authSource=admin&serverSelectionTimeoutMS=2000"

# 3) Arrancar
cd ms-cliente-gestion
./gradlew bootRun
```

Flyway crea la tabla `clientes` al arrancar. Si falta `JWT_SECRET` (o tiene menos de 32 caracteres), `ADMIN_USER` o `ADMIN_PASSWORD_HASH`, **la aplicación no arranca** y el mensaje dice cuál falta.

### Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `SPRING_R2DBC_URL` | Sí | URL R2DBC de PostgreSQL |
| `SPRING_FLYWAY_URL` | Sí | URL JDBC de PostgreSQL (solo para migrar al arrancar) |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` | Sí | Credenciales de PostgreSQL |
| `SPRING_DATA_MONGODB_URI` | Sí | URI de MongoDB (con `authSource` y `serverSelectionTimeoutMS`) |
| `JWT_SECRET` | Sí | Clave HS256, mínimo 32 caracteres |
| `JWT_EXPIRATION_MINUTES` | No (30) | Vigencia del token |
| `ADMIN_USER` | Sí | Usuario administrador |
| `ADMIN_PASSWORD_HASH` | Sí | Hash **BCrypt** de la contraseña (entre comillas simples en `.env`) |
| `SERVER_PORT` | No (8080) | Puerto HTTP |
| `LOG_LEVEL` | No (INFO) | Nivel de log raíz |

Para generar el hash BCrypt del administrador:

```bash
docker run --rm httpd:2.4-alpine htpasswd -nbBC 10 "" 'tu-contraseña' | cut -d: -f2
```

## 3. Cómo probarlo a mano

```bash
# Login: devuelve un token JWT
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usuario":"admin","password":"tu-contraseña"}' | jq -r .token)

# Crear, listar, obtener, actualizar y eliminar
curl -s -X POST localhost:8080/api/clientes -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"nombres":"Ana María","apellidos":"Pérez Loor","correo":"ana.perez@example.com","telefono":"0991234567"}'
curl -s localhost:8080/api/clientes -H "Authorization: Bearer $TOKEN"
```

| Método | Ruta | Respuesta |
|---|---|---|
| POST | `/auth/login` | 200 token · 401 credenciales inválidas |
| POST | `/clientes` | 201 + `Location` · 400 validación · 409 correo duplicado |
| GET | `/clientes` | 200 lista |
| GET | `/clientes/{id}` | 200 · 404 |
| PUT | `/clientes/{id}` | 200 · 400 · 404 · 409 |
| DELETE | `/clientes/{id}` | 204 · 404 |
| GET | `/actuator/health` | 200 `{"status":"UP"}` (sin detalles) |

**Contrato y Swagger:** el contrato OpenAPI (`src/main/resources/static/openapi/ms-cliente-gestion.yaml`) es la fuente de verdad. Con el sistema levantado, Swagger UI está en `http://localhost:8080/api/swagger-ui.html` (la documentación es pública; para probar los endpoints pulsa *Authorize* y pega el token del login).

Los errores usan `ProblemDetail` (RFC 7807). Sin token, `/clientes/**` responde 401.

## 4. Arquitectura

**Hexagonal (puertos y adaptadores)**, con el dominio en el centro y la dependencia siempre hacia adentro: `infrastructure → application → domain`. El detalle y los diagramas UML están en el [README raíz](../README.md#5-backend-ddd-y-arquitectura-hexagonal-reactiva).

```mermaid
flowchart LR
    C["adapter.in.web<br/>ClienteController · AuthController"] --> P["application.port.in<br/>casos de uso"]
    P --> S["application.service<br/>ClienteService"]
    S --> D["domain.cliente<br/>Cliente · Correo · Telefono"]
    S --> O["application.port.out<br/>ClienteRepositoryPort · AuditoriaPort"]
    O --> R["adapter.out.persistence<br/>R2DBC + PostgreSQL"]
    O --> A["adapter.out.audit<br/>MongoDB"]
    SEC["infrastructure.security<br/>JWT"] -.protege.-> C
```

```
src/main/java/com/eykcorp/clientes/
├── domain/cliente/            Cliente, Correo, Telefono y excepciones (Java puro)
├── application/
│   ├── port/in/               casos de uso (Mono/Flux)
│   ├── port/out/              ClienteRepositoryPort, AuditoriaPort
│   └── service/               ClienteService
└── infrastructure/
    ├── adapter/in/web/        controllers, DTOs, mappers, GlobalExceptionHandler
    ├── adapter/out/persistence/  R2DBC + Flyway (db/migration)
    ├── adapter/out/audit/     MongoDB
    └── security/              SecurityConfig, JwtService, AutenticadorAdministrador
```

Decisiones a conocer:

- **Reactivo de punta a punta:** WebFlux + R2DBC + MongoDB reactivo. Nada bloquea el *event loop*; el cálculo de BCrypt corre en `boundedElastic`.
- **Auditoría de mejor esfuerzo (EXC-2):** si MongoDB falla o tarda más de 2 s, el cambio sobre el cliente **se guarda igual** y el fallo se registra en el log sin datos personales.
- **Privacidad:** el documento de auditoría guarda solo `accion`, `clienteId` y `timestamp`; los `toString` de los objetos con datos personales están enmascarados.
- **Login sin caso de uso:** autenticar al único administrador es un mecanismo de seguridad de infraestructura, no una regla de negocio.
- **Sin rate limiting en el login:** queda fuera del alcance de esta versión.

## 5. Pruebas

```bash
cd ms-cliente-gestion
./gradlew check        # unitarias, integración, e2e, ArchUnit y reporte de cobertura
```

Los tests de integración y e2e usan **Testcontainers** (PostgreSQL y MongoDB reales), así que Docker debe estar en marcha.

| Tipo | Qué cubre |
|---|---|
| Dominio y servicio | Reglas de negocio con `StepVerifier` y dobles en memoria |
| Contrato | El mismo test corre contra el fake y contra PostgreSQL/MongoDB reales |
| Web (`@WebFluxTest`) | Cada código de estado y el mapeo de errores |
| Seguridad | 401/403, tokens inválidos o expirados, login |
| Arquitectura (ArchUnit) | El dominio es Java puro; la aplicación solo depende del dominio; sin ciclos; sin llamadas bloqueantes |
| E2E | Flujo completo con login real, PostgreSQL, MongoDB y Mongo caído |

Cobertura (JaCoCo): `build/reports/jacoco/test/html/index.html`.

## 6. Imagen Docker

El `Dockerfile` usa dos etapas: compila con JDK 17 y la imagen final solo lleva el JRE y el `.jar`, con usuario sin privilegios y `HEALTHCHECK` sobre `/actuator/health`.

```bash
docker build -t eykcorp/ms-cliente-gestion:0.1.0 .
```
