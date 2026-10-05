# syntax=docker/dockerfile:1
#
# Construcción en DOS ETAPAS (multi-stage). No son dos versiones del servicio:
#   1) "build"  -> imagen con el JDK completo, solo sirve para COMPILAR y empaquetar el .jar.
#   2) imagen final -> solo el JRE (lo mínimo para EJECUTAR) y el .jar copiado de la etapa 1.
# Resultado: una imagen final más pequeña y más segura (sin compilador ni código fuente).
# "-alpine" es la variante ligera de Linux (Alpine) de la imagen base.

# ---------- Etapa 1: compilar (JDK 17) ----------
FROM eclipse-temurin:17.0.20_8-jdk-alpine AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY src src
RUN ./gradlew --no-daemon bootJar -x test \
 && mv "$(ls build/libs/*.jar | grep -v -- '-plain.jar' | head -n 1)" app.jar

# ---------- Etapa 2: ejecutar (solo JRE 17) ----------
FROM eclipse-temurin:17.0.20_8-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
