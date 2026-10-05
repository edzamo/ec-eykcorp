# Excepciones a las reglas de arquitectura

El proyecto verifica su arquitectura con ArchUnit ([`ReglasArquitectura`](../../ms-cliente-gestion/src/test/java/com/eykcorp/clientes/ReglasArquitectura.java)). Tres puntos relajan la regla estricta. Se aplicaron **por defecto** para no bloquear el avance y **están pendientes de confirmación** del responsable del proyecto. Esta página explica qué se relaja, por qué, cómo se limita el riesgo y cómo se revierte.

| Id | Qué se relaja | Estado |
|---|---|---|
| EXC-1 | `application` usa Reactor (`reactor.core..`) | Aplicada, pendiente de confirmación |
| EXC-2 | La auditoría no comparte transacción con el guardado del cliente | Aplicada, pendiente de confirmación |
| Login | `POST /auth/login` no pasa por un caso de uso | Aplicada, pendiente de confirmación |
| EXC-3 | Lombok y SLF4J en `application` | **Autorizada por el titular del proyecto** (2026-10-05); implementada en la rama `feature/lombok-y-logs`, pendiente de fusionar a `main` |

## EXC-1 · Reactor en la capa de aplicación

**Regla que se relaja.** En una hexagonal estricta, el núcleo no depende de ninguna librería. Aquí `application` puede importar `reactor.core..` (`Mono`, `Flux`), y también `reactor.util.function`, `reactor.util.context` y `reactor.util.retry`. El dominio sigue siendo Java puro. Además, la regla de `application` admite la anotación `@Service` de Spring (visible en `ClienteService`).

**Por qué.** Los puertos devuelven `Mono` y `Flux` para que el flujo sea reactivo de punta a punta. Un puerto síncrono con adaptadores que conviertan perdería la no bloqueabilidad, que es uno de los objetivos del proyecto ([ADR-010](adr/adr-010-reactor-core-en-application.md), [ADR-002](adr/adr-002-webflux-r2dbc.md)).

**Mitigaciones.**
- La lista blanca es mínima y explícita (`REACTOR_EXC_1` en `ReglasArquitectura`); no se abre `reactor.util.*` completo.
- Se prohíben las llamadas bloqueantes (`Mono.block*`, `Flux.blockFirst`, `blockLast`, `toIterable`, `toStream`) en todo el código de producción.
- Los canarios de `ReglasArquitecturaTest` comprueban que `application` sigue rechazando Spring Data, validación, `@Transactional`, SLF4J y Lombok.

**Cómo revertirla.** Eliminar la constante `REACTOR_EXC_1` y las líneas `reactor.util.*` de la regla, y reescribir los puertos como síncronos. Es un cambio de fondo en todo el backend.

**Estado:** aplicada por defecto, pendiente de confirmación.

## EXC-2 · Auditoría sin transacción compartida

**Regla que se relaja.** Una operación sobre un cliente toca dos almacenes distintos (PostgreSQL y MongoDB). No hay transacción que los abarque: `ClienteService` no usa `@Transactional` y la regla de arquitectura lo rechaza en `application`. La auditoría se ejecuta **después** de guardar, con un tiempo límite de 2 segundos, y si falla solo se registra un aviso.

**Por qué.** Una transacción distribuida entre PostgreSQL y MongoDB sería compleja y desproporcionada para un PoC, y haría que un fallo de MongoDB impidiera operar clientes ([ADR-011](adr/adr-011-auditoria-mejor-esfuerzo.md)). El comportamiento está probado.

**Consecuencias que se aceptan.** Puede haber un cliente guardado sin su registro de auditoría (si MongoDB falla o tarda), y la auditoría no es una garantía fuerte de trazabilidad.

**Mitigaciones.**
- Tiempo límite de 2 segundos para que MongoDB no bloquee la respuesta (`ClienteService.TIMEOUT_AUDITORIA`).
- El fallo se registra con acción, id y tipo de error, sin datos personales.
- El estado de salud del servicio ignora a MongoDB (`management.health.mongo.enabled: false`), para que una auditoría caída no marque el servicio como `DOWN`.
- Pruebas: `ClienteServiceTest` (auditoría fallando y auditoría lenta) y `ClienteMongoCaidoE2ETest` (`201` dentro del tiempo límite y salud `UP` con MongoDB caído).

**Cómo revertirla.** Si se exige trazabilidad fuerte, pasar a un patrón de *outbox* o a la publicación por cola (la Épica 4 prevé SQS), lo que evita una transacción distribuida.

**Estado:** aplicada por defecto, pendiente de confirmación.

## Login sin caso de uso

**Regla que se relaja.** Todo comportamiento de entrada debería pasar por un puerto de entrada. `POST /auth/login` lo atiende `AuthController` directamente con `AutenticadorAdministrador` y `JwtService`, que viven en `infrastructure.security`. No existe un `LoginUseCase`.

**Por qué.** El login no es una regla de negocio del dominio de clientes: es un mecanismo de seguridad con un único administrador definido por variables de entorno. Crear un puerto y un servicio solo para comparar un hash BCrypt sería sobreingeniería ([ADR-007](adr/adr-007-jwt-usuario-unico.md)).

**Mitigaciones.**
- La regla `la_seguridad_no_depende_de_adaptadores` impide que la seguridad se acople a los adaptadores de persistencia o auditoría.
- La respuesta de login no distingue entre usuario inexistente y contraseña incorrecta, y limita la longitud de los campos (`SeguridadWebTest`).
- Nginx limita los intentos por IP.

**Cómo revertirla.** Si el alcance crece (varios usuarios, roles), promover el login a un puerto `CredencialesPort` con su caso de uso, como anticipa el [README principal, sección 9](../../README.md#9-seguridad-jwt).

**Estado:** aplicada por defecto, pendiente de confirmación.

## EXC-3 · Lombok y SLF4J en `application` (autorizada)

Hoy la regla es estricta: Lombok solo en `infrastructure` ([ADR-017](adr/adr-017-lombok-limitado-a-infraestructura.md)) y `application` registra con `System.Logger` (visible en `ClienteService`), cuyo puente a SLF4J se comprueba en `ClienteServiceLoggingTest`. La rama `feature/lombok-y-logs` explora permitir Lombok y SLF4J en `application`. Se documenta como [ADR-019](adr/adr-019-lombok-y-slf4j-en-application.md), en estado **Propuesta, en curso**. Si se acepta, se registrará como EXC-3. Mientras tanto, en `main` esa relajación no existe y los canarios `slf4j` y `lombok` de `ReglasArquitecturaTest` exigen que `application` las rechace.
