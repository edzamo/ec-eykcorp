# Épica 2 · Seguridad JWT

**Estado:** Hecha. **Rama de entrega:** `feature/seguridad-jwt` (el login y el guard del frontend llegaron con `feature/frontend-vue-crud-login`).

Protege la API con un token JWT (HS256) emitido a un único administrador definido por variables de entorno. El CRUD ya funcionaba antes de esta épica. Rutas de test de backend relativas a `ms-cliente-gestion/src/test/java/com/eykcorp/clientes/`.

### HU-E2-01 · Iniciar sesión
Como administrador quiero autenticarme con usuario y contraseña para obtener un token de acceso.

Criterios de aceptación
- Dadas credenciales correctas, cuando hago `POST /auth/login`, entonces responde `200` con un token y su vigencia en segundos (`expiraEn`).
- Dada una contraseña incorrecta, cuando hago `POST /auth/login`, entonces responde `401`.
- Dado un usuario inexistente o una contraseña errónea, cuando hago login, entonces el cuerpo de la respuesta es idéntico (no se revela qué dato falló).
- Dados campos en blanco o de más de 128 caracteres, cuando hago login, entonces responde `400`.

Rama de entrega: `feature/seguridad-jwt`
Evidencia: `infrastructure/adapter/in/web/SeguridadWebTest`, `infrastructure/security/AutenticadorAdministradorTest`, `JwtServiceTest`; `ClienteE2ETest` (`login_con_password_incorrecta_debe_responder_401`); `scripts/smoke-test.sh` (login correcto e incorrecto).

### HU-E2-02 · Proteger la API
Como responsable de seguridad quiero que solo las peticiones con un token válido lleguen a `/clientes` para que los datos no sean públicos.

Criterios de aceptación
- Dada una petición a `/clientes` sin token, con un token basura, expirado, firmado con otra clave, de otro emisor o con audiencia distinta, cuando se envía, entonces responde `401` con un `ProblemDetail`.
- Dado un token válido, cuando se envía, entonces responde `200`.
- Dada una ruta no declarada con un token válido, cuando se envía, entonces responde `403`; y sin token, `401`.
- Dados `POST /auth/login` y `GET /actuator/health`, entonces son los únicos puntos públicos.
- Dado `GET /actuator/env` sin token, entonces responde `401`.

Rama de entrega: `feature/seguridad-jwt`
Evidencia: `SeguridadWebTest`, `ClienteE2ETest` (`sin_token_o_con_token_invalido_debe_responder_401_en_todas_las_operaciones`), `ClientesApplicationTests`; `scripts/smoke-test.sh` (`GET /api/clientes sin token`).

### HU-E2-03 · Configuración segura del token
Como operador quiero que la aplicación no arranque con una configuración de seguridad débil para evitar despliegues inseguros.

Criterios de aceptación
- Dado un `JWT_SECRET` de menos de 32 caracteres o ausente, cuando arranca la aplicación, entonces falla en el arranque.
- Dado un token emitido, cuando se decodifica, entonces contiene `exp`, `iss` y `aud`, y el decodificador los exige.
- Dado `application.yml`, cuando lo reviso, entonces no contiene secretos por defecto.

Rama de entrega: `feature/seguridad-jwt`
Evidencia: `infrastructure/security/JwtPropertiesTest`, `JwtServiceTest`, `SeguridadWebTest`.

### HU-E2-04 · Sesión en la interfaz web
Como usuario quiero iniciar sesión en la web y que me devuelva a la pantalla de acceso si mi sesión caduca.

Criterios de aceptación
- Dado un usuario sin token, cuando abre `/`, entonces el router lo redirige a `/login`.
- Dado un token almacenado, cuando la SPA pide la lista, entonces envía `Authorization: Bearer <token>`.
- Dada una respuesta `401` en una operación distinta del login, cuando llega, entonces se borra el token y se redirige a `/login`.
- Dado un `401` del propio login (credenciales incorrectas), cuando llega, entonces se muestra el error sin cerrar sesión.
- Dado un usuario autenticado, cuando abre `/login`, entonces se le lleva a `/`.

Rama de entrega: `feature/frontend-vue-crud-login`
Evidencia: `tests/unit/app/router.test.js`, `tests/unit/app/App.integration.test.js` (MSW), `tests/unit/domain/useAuth.test.js`, `tests/unit/pages/LoginPage.test.js`, `tests/unit/services/httpClient.test.js`, `tests/unit/services/authService.test.js`.

### HU-E2-05 · Frenar la fuerza bruta en el acceso
Como responsable de seguridad quiero limitar los intentos de login para dificultar los ataques de adivinación.

Criterios de aceptación
- Dado Nginx, cuando una misma IP supera el límite de 5 peticiones por minuto (con ráfaga de 5) a `/api/auth/login`, entonces recibe `429`.

Rama de entrega: `feature/endurecimiento-infra`
Evidencia: `ms-cliente-presentacion/nginx.conf` (zona `limit_req_zone` y `limit_req_status 429`). No hay prueba automática de este límite; se verifica manualmente. El límite **no** existe en la aplicación (deuda declarada).
