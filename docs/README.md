# Documentación del proyecto

Esta carpeta amplía el [README principal](../README.md) con el detalle que no cabe en un solo documento: qué se pidió, cómo se ha repartido el trabajo, por qué la arquitectura es como es y cómo se comprueba que todo funciona. Todo el contenido describe la versión **v0.1.0** del repositorio.

## Por dónde empezar

| Si quieres... | Lee |
|---|---|
| Comprobar que se cumple lo pedido | [`requerimientos/requerimientos-practica.md`](requerimientos/requerimientos-practica.md), con su matriz de trazabilidad |
| Ver qué se construyó y con qué criterios de aceptación | [`historias/README.md`](historias/README.md) |
| Entender la arquitectura y sus decisiones | [`arquitectura/README.md`](arquitectura/README.md) y [`arquitectura/adr/README.md`](arquitectura/adr/README.md) |
| Saber qué reglas se relajaron y por qué | [`arquitectura/excepciones.md`](arquitectura/excepciones.md) |
| Ejecutar y valorar las pruebas | [`pruebas/estrategia.md`](pruebas/estrategia.md) y [`pruebas/resultados-v0.1.0.md`](pruebas/resultados-v0.1.0.md) |

## Contenido de cada carpeta

| Carpeta | Contenido | Pensada para |
|---|---|---|
| [`requerimientos/`](requerimientos/) | Requisitos de la parte práctica, requisitos añadidos por el proyecto y matriz requisito, implementación, evidencia y estado | Quien evalúa el cumplimiento |
| [`historias/`](historias/) | Historias de usuario por épica (E0 a E5), con criterios Dado/Cuando/Entonces, rama de entrega y evidencia | Quien quiere seguir el proceso de desarrollo |
| [`arquitectura/`](arquitectura/) | Visión hexagonal reactiva, excepciones a las reglas y registro de decisiones (ADR 001 a 019) | Quien revisa el diseño |
| [`pruebas/`](pruebas/) | Estrategia por niveles, comandos de ejecución, resultados verificados y deuda de pruebas | Quien quiere reproducir la verificación |

## Convenciones

- Los enlaces son relativos: la documentación se puede leer clonada o desde el repositorio.
- Cada afirmación sobre el código cita clases, archivos o ramas que existen en el repositorio.
- Lo que está **parcial o pendiente** se declara como tal (Épica 4 de AWS local, HTTPS, pruebas de navegador con Playwright, SonarQube, contrato OpenAPI). No se presenta como hecho.
- Tres decisiones se aplicaron por defecto y esperan confirmación del responsable del proyecto: EXC-1, EXC-2 y el login sin caso de uso. Están recogidas en [`arquitectura/excepciones.md`](arquitectura/excepciones.md).
