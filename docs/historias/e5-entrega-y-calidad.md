# Épica 5 · Entrega y calidad

**Estado:** Hecha, con deuda declarada. **Ramas de entrega:** `feature/endurecimiento-infra`, `refactor/renombrar-microservicios`, `refactor/lombok-infraestructura`, `fix/frontend-telefono-accesibilidad` y las ramas `docs/*` (por ejemplo `docs/readme-microservicios`).

Deja el proyecto listo para presentar: documentación, endurecimiento de la infraestructura, revisiones de calidad y seguridad y una prueba de humo del sistema completo.

### HU-E5-01 · README por microservicio
Como evaluador quiero que cada microservicio explique cómo ejecutarlo y probarlo para no tener que deducirlo del código.

Criterios de aceptación
- Dado `ms-cliente-gestion/README.md` y `ms-cliente-presentacion/README.md`, cuando los abro, entonces existen y están enlazados desde el [README principal, sección 14](../../README.md#14-cómo-ejecutar).
- Dado el README principal, cuando sigo "Cómo ejecutar" en un clon limpio, entonces el sistema arranca en `http://localhost:8080`.

Rama de entrega: `docs/readme-microservicios`
Evidencia: los dos README y el README principal.

### HU-E5-02 · Prueba de humo del sistema completo
Como evaluador quiero un único comando que compruebe el sistema ya levantado para validarlo en minutos.

Criterios de aceptación
- Dado el stack levantado con `docker compose up` y las variables `ADMIN_USER` y `ADMIN_PASSWORD`, cuando ejecuto `./scripts/smoke-test.sh`, entonces ejecuta 14 comprobaciones a través de Nginx (SPA, proxy, 401, login, CRUD, 400, 404 y 409).
- Dada una comprobación que falla, cuando termina el script, entonces sale con código distinto de cero e indica cuántas fallaron.
- Dada la ausencia de `ADMIN_USER` o `ADMIN_PASSWORD`, cuando lo ejecuto, entonces se detiene con un mensaje claro.

Rama de entrega: `feature/endurecimiento-infra`
Evidencia: `scripts/smoke-test.sh`; la lista completa está en [resultados v0.1.0](../pruebas/resultados-v0.1.0.md#prueba-de-humo).

### HU-E5-03 · Infraestructura endurecida
Como responsable de seguridad quiero contenedores sin privilegios y una superficie mínima expuesta para reducir el riesgo del despliegue.

Criterios de aceptación
- Dado el Compose, cuando lo reviso, entonces solo `ms-cliente-presentacion` publica un puerto (8080) y los cuatro servicios de la aplicación (`postgres`, `mongo`, `ms-cliente-gestion`, `ms-cliente-presentacion`) llevan `no-new-privileges`.
- Dadas las imágenes de los microservicios, cuando las ejecuto, entonces corren como usuario sin privilegios (`app` en el backend, `nginx-unprivileged` en el frontend).
- Dado Nginx, cuando responde a la SPA, entonces incluye las cabeceras `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy` y `Content-Security-Policy`.
- Dado `docker-compose.dev.yml`, cuando se usa, entonces las bases de datos se publican solo en `127.0.0.1`.

Rama de entrega: `feature/endurecimiento-infra` (CSP y accesibilidad en `fix/frontend-telefono-accesibilidad`)
Evidencia: `docker-compose.yml`, `docker-compose.dev.yml`, `ms-cliente-presentacion/nginx.conf`, ambos `Dockerfile`. Verificación por revisión; no hay prueba automática de las cabeceras.

### HU-E5-04 · Componentes con nombres claros y versionado
Como mantenedor quiero nombres de componente con un estándar y versiones por componente para identificar cada pieza y su imagen.

Criterios de aceptación
- Dados los microservicios, cuando los listo, entonces se llaman `ms-cliente-gestion` y `ms-cliente-presentacion`, con imágenes `eykcorp/ms-cliente-gestion:0.1.0` y `eykcorp/ms-cliente-presentacion:0.1.0`.
- Dada la versión del componente, cuando la reviso, entonces coincide en `build.gradle.kts` o `package.json` y en la etiqueta de la imagen.

Rama de entrega: `refactor/renombrar-microservicios`
Evidencia: `ms-cliente-gestion/build.gradle.kts` (`version = "0.1.0"`), `ms-cliente-presentacion/package.json`, `docker-compose.yml`; ver [ADR-016](../arquitectura/adr/adr-016-nombres-de-microservicios-y-semver.md).

### HU-E5-05 · Lombok solo en infraestructura
Como mantenedor quiero reducir código repetitivo sin contaminar el núcleo del sistema.

Criterios de aceptación
- Dado `domain` o `application`, cuando una clase usa Lombok, entonces la regla de ArchUnit falla.
- Dada la cobertura, cuando se ejecuta JaCoCo, entonces ignora el código generado por Lombok (`lombok.config`).

Rama de entrega: `refactor/lombok-infraestructura`
Evidencia: `ReglasArquitecturaTest` (canarios `debe_detectar_dominio_que_usa_lombok` y el escenario `lombok` de `debe_detectar_aplicacion_que_depende_de_infraestructura_prohibida`); `ms-cliente-gestion/lombok.config`; ver [ADR-017](../arquitectura/adr/adr-017-lombok-limitado-a-infraestructura.md).

### HU-E5-06 · Calidad verificada en cada cambio y deuda a la vista
Como evaluador quiero saber qué se verifica automáticamente y qué queda fuera para valorar el proyecto con realismo.

Criterios de aceptación
- Dado un pull request, cuando corre el CI, entonces ejecuta las pruebas con cobertura, ESLint, Prettier, `npm audit` (dependencias de producción, nivel alto) y la construcción de las imágenes.
- Dado el backend, cuando se ejecuta `./gradlew check`, entonces verifica un mínimo de 80 % de líneas y 70 % de ramas con JaCoCo.
- Dada la sección "Deuda conocida" del README principal, cuando la leo, entonces lista lo que falta: HTTPS, Playwright, adaptadores de la Épica 4, análisis de vulnerabilidades de Gradle en CI, SonarQube y límite de login en la aplicación.

Rama de entrega: `feature/endurecimiento-infra` y ramas `docs/*`
Evidencia: `.github/workflows/ci.yml`, `ms-cliente-gestion/build.gradle.kts` (`jacocoTestCoverageVerification`), [deuda de pruebas](../pruebas/resultados-v0.1.0.md#deuda-de-pruebas).
