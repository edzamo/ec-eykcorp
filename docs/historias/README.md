# Historias de usuario

El trabajo se organizó en seis épicas. Cada una tiene su archivo con las historias que la componen, los criterios de aceptación, la rama en la que se entregó y las pruebas que lo demuestran.

## Índice

| Épica | Archivo | Estado |
|---|---|---|
| E0 Fundación | [`e0-fundacion.md`](e0-fundacion.md) | Hecha |
| E1 CRUD de clientes | [`e1-crud-clientes.md`](e1-crud-clientes.md) | Hecha |
| E2 Seguridad JWT | [`e2-seguridad-jwt.md`](e2-seguridad-jwt.md) | Hecha |
| E3 Auditoría en MongoDB | [`e3-auditoria-mongodb.md`](e3-auditoria-mongodb.md) | Hecha |
| E4 AWS local (LocalStack) | [`e4-aws-local.md`](e4-aws-local.md) | **Parcial** |
| E5 Entrega y calidad | [`e5-entrega-y-calidad.md`](e5-entrega-y-calidad.md) | Hecha, con deuda declarada |

El plan original y el estado resumido están en el [README principal, sección 12](../../README.md#12-plan-de-entrega-épicas-e-historias). La trazabilidad completa requisito, implementación y evidencia está en la [matriz de requerimientos](../requerimientos/requerimientos-practica.md#3-matriz-de-trazabilidad).

## Formato de una historia

```
### HU-E1-01 · Título corto
Como <rol> quiero <capacidad> para <beneficio>.

Criterios de aceptación
- Dado <contexto>, cuando <acción>, entonces <resultado observable>.

Rama de entrega: <rama>
Evidencia: <clases de test o script>
```

- El identificador sigue el patrón `HU-E<épica>-<número>`.
- Los criterios son **verificables**: cada uno se puede comprobar con una prueba automática o con el script de humo, y la evidencia nombra dónde.
- Los nombres de clases de test son los reales del repositorio. Las rutas de backend son relativas a `ms-cliente-gestion/src/test/java/com/eykcorp/clientes/` y las de frontend a `ms-cliente-presentacion/tests/unit/`.

## Definición de hecho

Una historia se da por terminada cuando se cumplen todas estas condiciones:

1. Empezó con una prueba que fallaba (enfoque TDD: rojo, verde, refactor) y el historial de Git lo refleja con commits separados.
2. Sus criterios de aceptación están cubiertos por pruebas automáticas que pasan.
3. `./gradlew check` (backend) o `npm run test:coverage`, `npm run lint` y `npm run format:check` (frontend) pasan en local y en CI.
4. Las reglas de arquitectura de ArchUnit siguen en verde.
5. No hay secretos en el repositorio ni datos personales en los logs.
6. La documentación afectada (README del microservicio, ADR o esta carpeta) está actualizada.
7. Se entregó en su propia rama y se fusionó a `main` con `--no-ff` ([estrategia de ramas](../../README.md#132-estrategia-de-ramas-y-commits)).

Una historia que cumple solo una parte se marca como **parcial** y se detalla lo que falta, como ocurre en la [Épica 4](e4-aws-local.md).
