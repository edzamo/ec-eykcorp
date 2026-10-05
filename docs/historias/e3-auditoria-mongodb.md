# Épica 3 · Auditoría en MongoDB

**Estado:** Hecha. **Rama de entrega:** `feature/auditoria-mongodb`.

Cada cambio sobre un cliente deja una huella en MongoDB. Es una segunda persistencia, de **mejor esfuerzo**: si MongoDB falla, el cliente igualmente se guarda ([ADR-011](../arquitectura/adr/adr-011-auditoria-mejor-esfuerzo.md), [EXC-2](../arquitectura/excepciones.md)). Rutas de test relativas a `ms-cliente-gestion/src/test/java/com/eykcorp/clientes/`.

### HU-E3-01 · Registrar cada cambio de un cliente
Como auditor quiero que se registre cuándo se creó, actualizó o eliminó un cliente para reconstruir su historia.

Criterios de aceptación
- Dado un alta, una modificación o una baja correctas, cuando termina la operación, entonces se registra una auditoría con la acción `CREADO`, `ACTUALIZADO` o `ELIMINADO` respectivamente.
- Dada una operación que falla (correo duplicado, cliente inexistente), cuando termina con error, entonces no se registra auditoría.
- Dadas varias acciones sobre un cliente, cuando se consultan, entonces conservan su orden y no se mezclan con las de otros clientes.

Rama de entrega: `feature/auditoria-mongodb`
Evidencia: `application/service/ClienteServiceTest` (con `FakeAuditoriaPort`); `application/port/out/AuditoriaPortContract`, ejecutado por `FakeAuditoriaPortTest` y por `infrastructure/adapter/out/audit/AuditoriaMongoPublisherIT` (MongoDB real).

### HU-E3-02 · Auditoría sin datos personales
Como responsable de privacidad quiero que la auditoría no copie datos personales para minimizar la exposición.

Criterios de aceptación
- Dado un documento de auditoría guardado, cuando se lee en bruto, entonces contiene únicamente `_id`, `accion`, `clienteId`, `timestamp` y `_class`; no incluye correo, nombres ni teléfono.
- Dado un fallo de auditoría, cuando se registra en logs, entonces el mensaje incluye acción, id y tipo de error, pero no datos del cliente.

Rama de entrega: `feature/auditoria-mongodb`
Evidencia: `AuditoriaMongoPublisherIT`, `infrastructure/adapter/out/audit/AuditoriaDocumentMapperTest`, `application/service/ClienteServiceLoggingTest`.

### HU-E3-03 · Operar aunque MongoDB falle
Como usuario quiero seguir gestionando clientes aunque el almacén de auditoría no responda para no perder disponibilidad por un servicio secundario.

Criterios de aceptación
- Dada una auditoría que devuelve error, cuando se crea un cliente, entonces la operación termina bien y el cliente queda persistido.
- Dada una auditoría lenta, cuando se opera, entonces la operación no espera más del tiempo límite (2 segundos).
- Dado MongoDB caído, cuando se hace un `POST /clientes`, entonces responde `201` dentro del tiempo límite.
- Dado MongoDB caído, cuando se consulta `/actuator/health`, entonces el servicio sigue en `UP`.

Rama de entrega: `feature/auditoria-mongodb`
Evidencia: `ClienteServiceTest` (`con_auditoria_fallando_...`, `con_auditoria_lenta_...`), `ClienteMongoCaidoE2ETest`, `AuditoriaMongoPublisherTest`.

### HU-E3-04 · MongoDB con privilegios mínimos en Compose
Como operador quiero que la aplicación use un usuario de MongoDB con permisos solo sobre la base de auditoría para limitar el impacto de una fuga de credenciales.

Criterios de aceptación
- Dado el servicio `mongo` del Compose, cuando arranca, entonces crea un usuario de aplicación con permiso de lectura y escritura únicamente sobre la base `auditoria` (script `infra/mongo/init-auditoria.js`).
- Dada la conexión del backend, cuando se configura, entonces usa `MONGO_APP_USER` y no el usuario administrador (`SPRING_DATA_MONGODB_URI` en `docker-compose.yml`).
- Dado el Compose, cuando arranca el backend, entonces espera a que `mongo` esté sano (`depends_on` con `service_healthy`).

Rama de entrega: `feature/auditoria-mongodb` (usuario mínimo reforzado en `feature/endurecimiento-infra`)
Evidencia: `infra/mongo/Dockerfile`, `infra/mongo/init-auditoria.js`, `docker-compose.yml`; con el stack levantado, la auditoría se verificó con documentos `CREADO`, `ACTUALIZADO` y `ELIMINADO` (ver [README principal, sección 12](../../README.md#12-plan-de-entrega-épicas-e-historias)). El script `scripts/smoke-test.sh` no consulta MongoDB directamente.
