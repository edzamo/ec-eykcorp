# Épica 0 · Fundación

**Estado:** Hecha. **Rama principal de entrega:** `feature/fundacion-monorepo`.

Deja un repositorio ejecutable sobre el que construir: estructura de monorepo, entorno reproducible y verificación automática desde el primer commit.

### HU-E0-01 · Monorepo ordenado
Como evaluador quiero un repositorio con una estructura clara y sin archivos de entorno para clonarlo y orientarme sin esfuerzo.

Criterios de aceptación
- Dado un clon limpio, cuando listo la raíz, entonces veo `ms-cliente-gestion/`, `ms-cliente-presentacion/`, `docker-compose.yml`, `.env.example`, `scripts/`, `infra/` y `.github/workflows/`.
- Dado el archivo `.gitignore`, cuando creo un `.env` con secretos, entonces Git lo ignora.
- Dado el repositorio, cuando busco secretos, entonces solo existe `.env.example`, con valores vacíos para contraseñas y claves.

Rama de entrega: `feature/fundacion-monorepo`
Evidencia: revisión de la estructura del repositorio; `.env.example`.

### HU-E0-02 · Entorno reproducible con Compose
Como evaluador quiero levantar la base de datos con Docker Compose para no instalar nada más que Docker.

Criterios de aceptación
- Dado un `.env` sin `POSTGRES_PASSWORD`, cuando ejecuto `docker compose config`, entonces Compose falla indicando la variable obligatoria (usa la sintaxis `:?` en `docker-compose.yml`).
- Dado un `.env` completo, cuando levanto `postgres`, entonces su `healthcheck` con `pg_isready` pasa a sano.
- Dado que se reinicia el contenedor, cuando vuelve a arrancar, entonces los datos persisten en el volumen `pgdata`.

Rama de entrega: `feature/fundacion-monorepo` (la versión completa del Compose, con Nginx, llegó en `feature/docker-compose-nginx`)
Evidencia: `docker compose config`; `docker-compose.yml`.

### HU-E0-03 · Integración continua que compila
Como desarrollador quiero que cada cambio dispare una compilación y las pruebas para detectar roturas de inmediato.

Criterios de aceptación
- Dado un `push` a `main` o un pull request hacia `main`, cuando se dispara el workflow, entonces se ejecutan los jobs `ms-cliente-gestion`, `ms-cliente-presentacion` e `imagenes-docker`.
- Dado el job `ms-cliente-gestion`, cuando termina, entonces ha ejecutado `./gradlew check` con Java 17.
- Dado el job `imagenes-docker`, cuando termina, entonces `docker compose build` construyó las imágenes de los dos microservicios.

Rama de entrega: `feature/fundacion-monorepo`
Evidencia: `.github/workflows/ci.yml`.

### HU-E0-04 · Build reproducible
Como desarrollador quiero versiones fijas de dependencias y de toolchain para que el build dé el mismo resultado en cualquier máquina.

Criterios de aceptación
- Dado un JDK local distinto de 17, cuando ejecuto `./gradlew check`, entonces Gradle compila con el toolchain de Java 17 declarado en `build.gradle.kts`.
- Dado `docker-compose.yml` y los `Dockerfile`, cuando los reviso, entonces ninguna imagen usa la etiqueta `latest`.

Rama de entrega: `feature/fundacion-monorepo`
Evidencia: `ms-cliente-gestion/build.gradle.kts`, `ms-cliente-gestion/Dockerfile`, `ms-cliente-presentacion/Dockerfile`; ver [ADR-013](../arquitectura/adr/adr-013-gradle-toolchain-java-17.md).
