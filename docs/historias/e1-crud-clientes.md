# Épica 1 · CRUD de clientes

**Estado:** Hecha. **Ramas de entrega:** `feature/hexagonal-dominio`, `feature/hexagonal-servicios`, `feature/persistencia-postgres`, `feature/api-rest-clientes`, `feature/frontend-vue-crud-login`, `feature/docker-compose-nginx`.

Es el entregable mínimo del encargo: gestionar clientes de punta a punta, desde el dominio hasta la interfaz web servida por Nginx. Las rutas de test de backend son relativas a `ms-cliente-gestion/src/test/java/com/eykcorp/clientes/`.

### HU-E1-01 · Dominio de cliente con invariantes
Como responsable del negocio quiero que un cliente nunca pueda existir con datos inválidos para confiar en la información almacenada.

Criterios de aceptación
- Dado nombres o apellidos vacíos o de más de 100 caracteres, cuando se crea un `Cliente`, entonces se lanza `ValorInvalidoException`.
- Dado un correo sin formato válido, cuando se construye un `Correo`, entonces se rechaza; y dado un correo válido con mayúsculas o espacios, entonces se guarda recortado y en minúsculas.
- Dado un teléfono que no tenga entre 7 y 15 dígitos (con `+` inicial opcional), cuando se construye un `Telefono`, entonces se rechaza; y el teléfono ausente (`null`) es válido.
- Dado un cliente existente, cuando se actualizan sus datos con `conDatos`, entonces la fecha de creación no cambia.

Rama de entrega: `feature/hexagonal-dominio`
Evidencia: `domain/cliente/ClienteTest`, `CorreoTest`, `TelefonoTest`; regla `el_dominio_es_java_puro` de `ArchitectureTest`.

### HU-E1-02 · Casos de uso reactivos
Como desarrollador quiero un caso de uso por intención de negocio para poder probar la lógica sin infraestructura.

Criterios de aceptación
- Dado un cliente válido y un correo libre, cuando se ejecuta `crear`, entonces se guarda con id y con la fecha del reloj inyectado, y se registra la auditoría `CREADO`.
- Dado un correo ya registrado, cuando se ejecuta `crear` o `actualizar`, entonces el flujo termina con `CorreoDuplicadoException` y no se audita.
- Dado un id inexistente, cuando se ejecuta `obtener`, `actualizar` o `eliminar`, entonces el flujo termina con `ClienteNoEncontradoException`.
- Dado un cliente que se actualiza conservando su propio correo, cuando se ejecuta `actualizar`, entonces no se considera duplicado.

Rama de entrega: `feature/hexagonal-servicios`
Evidencia: `application/service/ClienteServiceTest` (con `StepVerifier`, sobre `InMemoryClienteRepository` y `FakeAuditoriaPort`); regla `la_aplicacion_no_depende_de_adaptadores_ni_frameworks` de `ArchitectureTest`.

### HU-E1-03 · Persistencia en PostgreSQL
Como operador quiero que los clientes se guarden en PostgreSQL con el esquema versionado para que el despliegue sea reproducible.

Criterios de aceptación
- Dada una base vacía, cuando arranca la aplicación, entonces Flyway aplica `V1__crear_tabla_clientes.sql` y existe la tabla `clientes` con la restricción única `uk_clientes_correo`.
- Dado el contrato del repositorio, cuando se ejecuta contra `InMemoryClienteRepository` y contra PostgreSQL real, entonces ambos lo cumplen (`guardar`, `buscarPorId`, `buscarTodos`, `eliminarPorId`, `existePorCorreo`, `existePorCorreoDeOtro`).
- Dos altas simultáneas con el mismo correo, cuando chocan con la restricción única, entonces una gana y la otra recibe `CorreoDuplicadoException`.

Rama de entrega: `feature/persistencia-postgres`
Evidencia: `application/port/out/ClienteRepositoryPortContract` ejecutado por `InMemoryClienteRepositoryTest` y por `infrastructure/adapter/out/persistence/ClientePersistenceAdapterIT`; `ClienteEntityMapperTest`.

### HU-E1-04 · API REST con errores consistentes
Como consumidor de la API quiero respuestas predecibles, también en los errores, para tratarlas sin adivinar.

Criterios de aceptación
- Dado un `POST /clientes` válido, cuando se envía, entonces responde `201` con la cabecera `Location`.
- Dado un cuerpo con campos inválidos, cuando se envía, entonces responde `400` con un `ProblemDetail` que incluye los errores por campo.
- Dado un correo duplicado, cuando se crea o se actualiza, entonces responde `409`.
- Dado un id inexistente, cuando se consulta, actualiza o elimina, entonces responde `404`; y un id no numérico responde `400`.
- Dado un `DELETE` correcto, cuando se envía, entonces responde `204`.
- Dado un error inesperado, cuando ocurre, entonces responde `500` sin filtrar detalles internos.
- Dado un JSON malformado, cuando se envía, entonces responde `400`.

Rama de entrega: `feature/api-rest-clientes`
Evidencia: `infrastructure/adapter/in/web/ClienteControllerTest` (`@WebFluxTest`), `GlobalExceptionHandlerTest`, `ClienteWebMapperTest`; `ClienteE2ETest` (flujo completo contra PostgreSQL y MongoDB reales); `scripts/smoke-test.sh`.

### HU-E1-05 · Interfaz web del CRUD
Como usuario quiero listar, crear, editar y eliminar clientes desde el navegador, y ver qué ocurre mientras espero o cuando algo falla.

Criterios de aceptación
- Dada la pantalla de clientes, cuando se está cargando la lista, entonces se muestra el indicador de carga (`LoadingSpinner`).
- Dado un fallo al cargar o al eliminar, cuando ocurre, entonces se muestra el mensaje en `ErrorAlert`.
- Dado un error `400` o `409` al crear o editar, cuando se envía el formulario, entonces el error se muestra junto al formulario, no como alerta de página.
- Dado el botón eliminar, cuando se pulsa, entonces se pide confirmación (`ConfirmDialog`) antes de borrar.
- Dado un cliente creado, cuando termina la operación, entonces aparece en la tabla sin recargar la página.
- Dado un formulario con el teléfono vacío, cuando se envía, entonces el teléfono viaja como `null` y se acepta (corrección en `fix/frontend-telefono-accesibilidad`).

Rama de entrega: `feature/frontend-vue-crud-login` (más `fix/frontend-telefono-accesibilidad`)
Evidencia: `tests/unit/pages/ClientesPage.test.js`, `tests/unit/components/ClienteForm.test.js`, `ClienteTable.test.js`, `ConfirmDialog.test.js`, `ErrorAlert.test.js`, `LoadingSpinner.test.js`, `tests/unit/domain/useClientes.test.js`, `tests/unit/services/clienteService.test.js`, `tests/unit/app/App.integration.test.js` (con MSW).

### HU-E1-06 · Un solo comando para todo el sistema (Nginx y Docker)
Como evaluador quiero levantar toda la aplicación con `docker compose up --build` y usarla en un único puerto.

Criterios de aceptación
- Dado un `.env` completo, cuando ejecuto `docker compose up --build`, entonces arrancan `postgres`, `mongo`, `ms-cliente-gestion` y `ms-cliente-presentacion`.
- Dado el stack levantado, cuando abro `http://localhost:8080/`, entonces Nginx sirve la SPA; y cualquier ruta del router vuelve a `index.html`.
- Dada una petición a `/api/...`, cuando llega a Nginx, entonces se reenvía al backend quitando el prefijo `/api`.
- Dado el host, cuando reviso los puertos publicados, entonces solo está el 8080 de Nginx; backend y bases de datos no se publican.
- Dadas las imágenes de los microservicios, cuando las inspecciono, entonces se construyeron en varias etapas y no se ejecutan como root.

Rama de entrega: `feature/docker-compose-nginx`
Evidencia: `scripts/smoke-test.sh` (comprobaciones `GET /` y `GET /api/actuator/health`); job `imagenes-docker` de CI; `docker-compose.yml`, `ms-cliente-presentacion/nginx.conf`.
