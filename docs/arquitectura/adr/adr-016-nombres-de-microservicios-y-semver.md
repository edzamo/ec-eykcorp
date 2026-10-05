# ADR-016 · Nombres ms-dominio-subdominio y versionado SemVer por componente

**Estado:** Aceptada

## Contexto

Los componentes necesitaban nombres que indicaran dominio y propósito, y una forma de versionarlos de manera independiente.

## Decisión

Los dos microservicios se llaman `ms-cliente-gestion` (backend) y `ms-cliente-presentacion` (frontend). Cada uno lleva su propia versión SemVer (`0.1.0`), declarada en `build.gradle.kts` y `package.json` y reutilizada como etiqueta de la imagen Docker (`eykcorp/ms-cliente-gestion:0.1.0`). El paquete Java raíz sigue siendo `com.eykcorp.clientes`.

## Consecuencias

- Los nombres explican qué hace cada pieza.
- El renombrado se hizo en `refactor/renombrar-microservicios` y obligó a actualizar rutas, Compose y CI.

## Alternativas descartadas

- Nombres genéricos (`backend`, `frontend`) o con `crud`: poco informativos.

[Volver al índice de ADR](README.md)
