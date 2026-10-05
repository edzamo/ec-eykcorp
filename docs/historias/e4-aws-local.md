# Épica 4 · AWS local (LocalStack)

**Estado: PARCIAL.** **Rama de entrega:** `feature/aws-localstack`.

El objetivo era simular AWS sin una cuenta real: LocalStack en el Compose, aprovisionamiento automático de recursos y que el backend usara Secrets Manager y SQS a través de adaptadores de salida. Está hecha la **infraestructura simulada y el despliegue de la SPA a S3**; **faltan los dos adaptadores del backend**.

| Pieza | Estado |
|---|---|
| Servicio `localstack` (4.4.0, sin token) en el Compose con el perfil `aws` | Hecho y probado |
| Aprovisionamiento: bucket S3, cola SQS `auditoria-clientes`, secreto en Secrets Manager | Hecho y probado |
| Despliegue de la SPA a S3 simulado | Hecho y probado |
| Backend leyendo el secreto desde Secrets Manager | **Pendiente** (los secretos llegan por variables de entorno) |
| `AuditoriaSqsPublisher` y consumidor (auditoría por SQS) | **Pendiente** (la auditoría va directo a MongoDB) |
| Tests de integración del perfil `aws` en CI | **Pendiente** (el CI actual no levanta LocalStack) |

### HU-E4-01 · AWS simulado en el Compose (HECHA)
Como evaluador quiero activar un AWS simulado con un perfil de Compose para ver la integración sin crear una cuenta.

Criterios de aceptación
- Dado `docker compose up -d` sin perfil, cuando arranca, entonces LocalStack no se inicia.
- Dado `docker compose --profile aws up -d localstack`, cuando arranca, entonces LocalStack 4.4.0 queda sano y expone la API en `127.0.0.1:4566` con los servicios `s3`, `sqs` y `secretsmanager`.
- Dada la imagen fijada en 4.4.0, cuando se ejecuta, entonces no exige token de cuenta ([ADR-014](../arquitectura/adr/adr-014-localstack-sin-token.md)).

Rama de entrega: `feature/aws-localstack`
Evidencia: `docker-compose.yml` (servicio `localstack`); verificación manual descrita en el [README principal, sección 10.5](../../README.md#105-simulación-de-aws-en-local-localstack).

### HU-E4-02 · Recursos AWS aprovisionados automáticamente (HECHA)
Como desarrollador quiero que los recursos AWS se creen solos al arrancar LocalStack para no hacerlo a mano.

Criterios de aceptación
- Dado LocalStack recién iniciado, cuando ejecuta los scripts de `ready.d`, entonces existen el bucket `eykcorp-clientes-web` (con hosting estático), la cola `auditoria-clientes` y el secreto `eykcorp/clientes/jwt-secret`.

Rama de entrega: `feature/aws-localstack`
Evidencia: `infra/localstack/init/ready.d/01-recursos.sh`. Verificación manual; no hay prueba automática.

### HU-E4-03 · Desplegar la SPA a S3 simulado (HECHA)
Como evaluador quiero publicar la SPA compilada en un bucket S3 simulado para ver la variante de despliegue estático.

Criterios de aceptación
- Dado LocalStack con el perfil `aws`, cuando ejecuto `./scripts/aws-local-deploy-frontend.sh`, entonces compila la SPA, sincroniza `dist/` con `s3://eykcorp-clientes-web` y lista su contenido.
- Dado el despliegue terminado, cuando abro `http://localhost:4566/eykcorp-clientes-web/index.html`, entonces se sirve la SPA.

Rama de entrega: `feature/aws-localstack`
Evidencia: `scripts/aws-local-deploy-frontend.sh`. Verificación manual.

### HU-E4-04 · Backend lee secretos desde Secrets Manager (PENDIENTE)
Como operador quiero que el backend obtenga `JWT_SECRET` y las credenciales de base de datos desde Secrets Manager para no depender de variables de entorno planas.

Criterios de aceptación (por cumplir)
- Dado el perfil `aws` de Spring activo, cuando arranca el backend, entonces lee el secreto desde el endpoint indicado por `AWS_ENDPOINT_URL`.
- Dado el perfil `aws` desactivado, cuando arranca el backend, entonces usa las variables de entorno como hoy.
- Dado un adaptador de salida nuevo, cuando se añade, entonces las reglas de ArchUnit siguen en verde y el dominio no cambia.

Rama de entrega: sin entregar.
Evidencia: ninguna todavía. El secreto existe en LocalStack, pero nada en el backend lo consume.

### HU-E4-05 · Auditoría por SQS (PENDIENTE)
Como arquitecto quiero que la auditoría se publique en una cola SQS y un consumidor la guarde en MongoDB para desacoplar la escritura del flujo principal.

Criterios de aceptación (por cumplir)
- Dado el perfil `aws`, cuando se crea un cliente, entonces `AuditoriaSqsPublisher` (implementación de `AuditoriaPort`) publica un mensaje en `auditoria-clientes`.
- Dado un mensaje en la cola, cuando lo recibe el consumidor, entonces guarda el documento de auditoría en MongoDB.
- Dado el perfil `aws` desactivado, cuando se crea un cliente, entonces la auditoría sigue yendo directa a MongoDB.
- Dado el CI, cuando corre las pruebas de integración del perfil `aws`, entonces las ejecuta contra LocalStack.

Rama de entrega: sin entregar.
Evidencia: ninguna todavía. La clase `AuditoriaSqsPublisher` no existe en el repositorio.
