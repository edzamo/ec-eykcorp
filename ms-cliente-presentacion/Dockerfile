# syntax=docker/dockerfile:1
#
# Construcción en DOS ETAPAS (multi-stage):
#   1) "build"  -> Node, solo para compilar la SPA de Vue a archivos estáticos.
#   2) imagen final -> Nginx, que sirve esos archivos y hace de proxy hacia el backend.
# Node no viaja en la imagen final: solo los archivos ya compilados.

# ---------- Etapa 1: compilar (Node 22) ----------
FROM node:22.23.3-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# ---------- Etapa 2: servir (Nginx sin privilegios) ----------
# Imagen oficial de Nginx que NO corre como root (escucha en el puerto 8080 del contenedor).
FROM nginxinc/nginx-unprivileged:1.31.6-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --retries=5 \
  CMD wget -qO- http://localhost:8080/ >/dev/null || exit 1
