# ADR-005 · Nginx como servidor de la SPA y reverse proxy

**Estado:** Aceptada

## Contexto

El navegador debe hablar con la SPA y con la API. Servirlas desde orígenes distintos obligaría a configurar CORS y a publicar más puertos.

## Decisión

Nginx sirve los archivos estáticos de la SPA y reenvía `/api/` a `ms-cliente-gestion`, quitando el prefijo. Es el único servicio que publica un puerto (8080). Añade cabeceras de seguridad y un límite de intentos de login (5 por minuto por IP). Ver `ms-cliente-presentacion/nginx.conf`.

## Consecuencias

- Mismo origen: no hace falta CORS en producción.
- Backend y bases de datos quedan accesibles solo dentro de la red de Compose.
- El TLS no está implementado; se terminaría delante o en este mismo Nginx (pendiente).

## Alternativas descartadas

- Exponer el backend directamente: obliga a configurar CORS y aumenta la superficie expuesta.

[Volver al índice de ADR](README.md)
