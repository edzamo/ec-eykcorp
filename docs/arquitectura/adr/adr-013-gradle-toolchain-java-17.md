# ADR-013 · Gradle (Kotlin DSL) con toolchain de Java 17

**Estado:** Aceptada

## Contexto

El encargo pide Java 17 y el build debe ser reproducible aunque el JDK local sea otro.

## Decisión

El build usa Gradle con Kotlin DSL y el wrapper versionado. `java.toolchain` fija la versión 17 en `build.gradle.kts`. Spring Boot, plugins y dependencias llevan versión exacta, y las versiones transitivas con CVE se fijan explícitamente.

## Consecuencias

- Compila siempre con Java 17.
- Builds incrementales y wrapper en el repositorio.
- Las versiones fijadas hay que actualizarlas a mano (Dependabot ayuda).

## Alternativas descartadas

- Maven: válido, pero sin un toolchain tan directo.

[Volver al índice de ADR](README.md)
