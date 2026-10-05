#!/usr/bin/env bash
# Genera el archivo .env para levantar el sistema por primera vez.
#
#  - Contraseñas de PostgreSQL y MongoDB y secreto JWT: ALEATORIOS en cada instalación.
#  - Usuario administrador de la aplicación: el de DEMOSTRACIÓN documentado en el README
#      usuario:    admin
#      contraseña: Eyk-Demo-2026
#    SOLO para uso local. Para cualquier otro entorno define tu propia contraseña:
#      ADMIN_PASSWORD='otra-contraseña' ./scripts/init-env.sh --force
#
# Requiere Docker (se usa para calcular el hash BCrypt) y openssl.
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
DESTINO="${ENV_FILE:-$RAIZ/.env}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Eyk-Demo-2026}"

if [ -e "$DESTINO" ] && [ "${1:-}" != "--force" ]; then
  echo "Ya existe $DESTINO; no lo sobrescribo. Usa --force para regenerarlo." >&2
  exit 1
fi
command -v docker >/dev/null || { echo "Necesito Docker para calcular el hash BCrypt." >&2; exit 1; }
command -v openssl >/dev/null || { echo "Necesito openssl para generar secretos." >&2; exit 1; }

# Hash BCrypt (coste 10). htpasswd genera "$2y$"; Spring Security lo acepta, pero se normaliza a "$2a$".
HASH="$(docker run --rm httpd:2.4-alpine htpasswd -nbBC 10 "" "$ADMIN_PASSWORD" | cut -d: -f2 | tr -d '\n' | sed 's/^\$2y/\$2a/')"

umask 077
cat > "$DESTINO" <<ENV
# Generado por scripts/init-env.sh. NO commitear (está en .gitignore).
POSTGRES_DB=clientes
POSTGRES_USER=clientes_app
POSTGRES_PASSWORD=$(openssl rand -hex 24)

MONGO_USER=mongo_root
MONGO_PASSWORD=$(openssl rand -hex 24)
MONGO_APP_USER=auditoria_app
MONGO_APP_PASSWORD=$(openssl rand -hex 24)

JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
JWT_EXPIRATION_MINUTES=15

ADMIN_USER=$ADMIN_USER
ADMIN_PASSWORD_HASH='$HASH'

LOG_LEVEL=INFO
ENV

echo "Listo: $DESTINO"
echo "  Usuario de la aplicación:    $ADMIN_USER"
if [ "$ADMIN_PASSWORD" = "Eyk-Demo-2026" ]; then
  echo "  Contraseña (demo, solo local): $ADMIN_PASSWORD"
else
  echo "  Contraseña: la que indicaste en ADMIN_PASSWORD"
fi
echo "Siguiente paso:  docker compose up --build"
