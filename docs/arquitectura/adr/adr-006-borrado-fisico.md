# ADR-006 · Borrado físico de clientes

**Estado:** Aceptada

## Contexto

El encargo habla de "eliminar" un cliente. Hay que decidir si el registro desaparece o se marca como borrado.

## Decisión

`DELETE /clientes/{id}` elimina la fila (`eliminarPorId`) y responde `204`; si el cliente no existe, responde `404`. La baja queda registrada en la auditoría con la acción `ELIMINADO`.

## Consecuencias

- Modelo simple: sin columna de estado ni filtros en cada consulta.
- El dato se pierde de la tabla principal; la auditoría conserva solo el id y la acción, sin datos personales.
- Añadir borrado lógico más adelante es un cambio acotado.

## Alternativas descartadas

- Borrado lógico: complica consultas y la unicidad del correo sin que el encargo lo pida.

[Volver al índice de ADR](README.md)
