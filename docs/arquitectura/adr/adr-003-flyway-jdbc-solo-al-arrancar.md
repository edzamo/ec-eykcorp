# ADR-003 · Flyway con JDBC solo al arrancar

**Estado:** Aceptada

## Contexto

R2DBC no incluye migraciones de esquema. Hace falta un esquema versionado y reproducible, y el acceso a datos en ejecución debe seguir siendo no bloqueante.

## Decisión

Flyway migra por JDBC únicamente durante el arranque (`flyway-core` y `flyway-database-postgresql`; `spring-jdbc` y el driver `postgresql` como dependencias de ejecución). La URL se configura aparte (`SPRING_FLYWAY_URL`). La tabla se crea con `V1__crear_tabla_clientes.sql`.

## Consecuencias

- El esquema es reproducible y está versionado junto al código.
- Existen dos URL de conexión (R2DBC y JDBC) que mantener alineadas en el Compose.
- JDBC se usa solo en el arranque; en ejecución todo es R2DBC.

## Alternativas descartadas

- Scripts SQL manuales: no reproducibles.
- Crear el esquema desde R2DBC propio: reinventa una herramienta madura.

[Volver al índice de ADR](README.md)
