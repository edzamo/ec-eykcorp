# ADR-011 · Auditoría de mejor esfuerzo

**Estado:** Aceptada, pendiente de confirmación (EXC-2)

## Contexto

Cada operación toca PostgreSQL y MongoDB. Un fallo o una lentitud de MongoDB no debería impedir operar clientes, y una transacción distribuida entre ambos sería desproporcionada.

## Decisión

`ClienteService` persiste el cambio y después audita, con un tiempo límite de 2 segundos. Si la auditoría falla o tarda, se registra un aviso (sin datos personales) y la operación termina bien. No hay `@Transactional` en `application`. El estado de salud ignora a MongoDB. Detalle en [EXC-2](../excepciones.md#exc-2--auditoría-sin-transacción-compartida).

## Consecuencias

- El servicio sigue disponible aunque MongoDB caiga (`ClienteMongoCaidoE2ETest`).
- Puede haber clientes sin su registro de auditoría: no es trazabilidad fuerte.
- Una salida futura es una cola (Épica 4, pendiente) o un patrón outbox.

## Alternativas descartadas

- Auditoría transaccional entre PostgreSQL y MongoDB: complejidad excesiva.

[Volver al índice de ADR](README.md)
