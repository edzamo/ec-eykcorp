# ADR-008 · MongoDB solo para la auditoría

**Estado:** Aceptada

## Contexto

Se quería mostrar un segundo adaptador de salida con otra tecnología. Un documento de auditoría (acción, cliente, instante) es el caso donde un almacén de documentos aporta algo natural.

## Decisión

MongoDB (Spring Data Reactive MongoDB) guarda un documento por cada alta, modificación o baja, a través de `AuditoriaPort` y su adaptador `AuditoriaMongoPublisher`. El documento contiene `accion`, `clienteId` y `timestamp`, sin datos personales. En el Compose, un usuario de aplicación solo tiene permisos sobre la base `auditoria`.

## Consecuencias

- Demuestra la intercambiabilidad de adaptadores (`AuditoriaPortContract` se ejecuta contra un fake y contra MongoDB real).
- Añade un servicio más a operar.
- Los clientes siguen en PostgreSQL.

## Alternativas descartadas

- Guardar los clientes en MongoDB: sin justificación frente a PostgreSQL.

[Volver al índice de ADR](README.md)
