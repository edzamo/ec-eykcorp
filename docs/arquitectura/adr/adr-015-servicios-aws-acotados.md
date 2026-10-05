# ADR-015 · Servicios AWS acotados a Secrets Manager, SQS y S3

**Estado:** Aceptada (implementación parcial, Épica 4)

## Contexto

Interesa simular los servicios de AWS que tengan un uso real en la aplicación y estén disponibles sin licencia de pago.

## Decisión

Se emulan Secrets Manager (secretos), SQS (cola de auditoría) y S3 (hosting estático de la SPA). El aprovisionamiento y el despliegue de la SPA a S3 están hechos. Los adaptadores del backend para Secrets Manager y SQS **están pendientes**. RDS y ECS/Fargate no se emulan.

## Consecuencias

- La parte de infraestructura simulada es verificable.
- Queda trabajo por hacer en el backend para completar la épica (ver [E4](../../historias/e4-aws-local.md)).
- PostgreSQL y los contenedores siguen en Docker Compose.

## Alternativas descartadas

- Emular RDS y ECS: requiere un plan de pago de LocalStack.

[Volver al índice de ADR](README.md)
