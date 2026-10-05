# ADR-009 · Español para dominio y documentación, inglés para infraestructura

**Estado:** Aceptada

## Contexto

El contrato de la API define los campos en español (`nombres`, `apellidos`, `correo`, `telefono`, `fechaCreacion`) y el evaluador lee en español.

## Decisión

El dominio, los casos de uso, los DTO y la documentación se escriben en español (`Cliente`, `Correo`, `CrearClienteUseCase`). Los nombres de infraestructura y de las tecnologías mantienen su forma habitual en inglés (`adapter`, `persistence`, `security`, `ClienteR2dbcRepository`).

## Consecuencias

- El lenguaje del código coincide con el lenguaje del negocio y del contrato.
- Mezcla controlada de idiomas en los nombres de paquete técnicos.

## Alternativas descartadas

- Todo en inglés: se aleja del contrato definido en español.

[Volver al índice de ADR](README.md)
