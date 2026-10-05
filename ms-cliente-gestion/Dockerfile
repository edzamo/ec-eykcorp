# syntax=docker/dockerfile:1
#
# Construcción en DOS ETAPAS (multi-stage). No son dos versiones del servicio:
#   1) "build"  -> imagen con el JDK completo, solo sirve para COMPILAR y empaquetar el .jar.
#   2) imagen final -> solo el JRE (lo mínimo para EJECUTAR) y el .jar copiado de la etapa 1.
# Resultado: una imagen final más pequeña y más segura (sin compilador ni código fuente).
# Se usan las variantes "noble" (Ubuntu) porque son multiplataforma (amd64 y arm64, p. ej.
# Mac con Apple Silicon); las "-alpine" de Temurin solo existen para amd64.

# ---------- Etapa 1: compilar (JDK 17) ----------
FROM eclipse-temurin:17.0.20_8-jdk-noble AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY src src
RUN ./gradlew --no-daemon bootJar -x test \
 && mv "$(ls build/libs/*.jar | grep -v -- '-plain.jar' | head -n 1)" app.jar

# ---------- Etapa 2: ejecutar (solo JRE 17) ----------
FROM eclipse-temurin:17.0.20_8-jre-noble
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
