# ADR-004 · Vue 3 con Vite para la SPA

**Estado:** Aceptada

## Contexto

El encargo pide una SPA con Vue o Astro. La aplicación es una interfaz interactiva (formulario, tabla, sesión), no contenido estático.

## Decisión

El frontend usa Vue 3 con Composition API, Vite y JavaScript, más Bootstrap 5 para la interfaz. Se organiza en `components`, `pages`, `domain` (composables), `services` y `app`.

## Consecuencias

- Servidor de desarrollo rápido y compilación sencilla (`npm run build`).
- Pruebas con Vitest y Vue Test Utils sobre la misma configuración de Vite.
- Sin TypeScript: los tipos se documentan con JSDoc y se comprueban con pruebas.

## Alternativas descartadas

- Astro: orientado a contenido estático; no aporta ventaja en una aplicación con estado y sesión.

[Volver al índice de ADR](README.md)
