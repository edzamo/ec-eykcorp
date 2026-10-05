#!/usr/bin/env bash
# Prueba de humo del sistema completo a través de Nginx (puerto 8080).
# Requiere el stack levantado (docker compose up) y estas variables:
#   ADMIN_USER, ADMIN_PASSWORD   (la contraseña EN CLARO cuyo BCrypt está en ADMIN_PASSWORD_HASH)
# Opcional: BASE_URL (por defecto http://localhost:8080)
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8080}"
: "${ADMIN_USER:?defina ADMIN_USER}" "${ADMIN_PASSWORD:?defina ADMIN_PASSWORD}"
fallos=0
ok()   { printf '  \033[32mOK\033[0m   %s\n' "$1"; }
fallo(){ printf '  \033[31mFALLO\033[0m %s\n' "$1"; fallos=$((fallos+1)); }
esperar() { # descripción esperado real
  if [ "$2" = "$3" ]; then ok "$1 -> $3"; else fallo "$1 (esperado $2, obtenido $3)"; fi
}
http() { curl -s -o /tmp/smoke_body -w '%{http_code}' "$@"; }

echo "Frontend y proxy"
esperar "GET / (SPA)" 200 "$(http "$BASE_URL/")"
esperar "GET /api/actuator/health" 200 "$(http "$BASE_URL/api/actuator/health")"

echo "Seguridad"
esperar "GET /api/clientes sin token" 401 "$(http "$BASE_URL/api/clientes")"
esperar "Login con contraseña incorrecta" 401 "$(http -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' -d "{\"usuario\":\"$ADMIN_USER\",\"password\":\"incorrecta\"}")"
codigo=$(http -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' -d "{\"usuario\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASSWORD\"}")
esperar "Login correcto" 200 "$codigo"
TOKEN=$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' /tmp/smoke_body)
[ -n "$TOKEN" ] || { fallo "no se obtuvo token"; exit 1; }
AUTH=(-H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json')

echo "CRUD de clientes"
CORREO="smoke.$(date +%s)@example.com"
esperar "POST /clientes (con teléfono)" 201 "$(http -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d "{\"nombres\":\"Ana\",\"apellidos\":\"Prueba\",\"correo\":\"$CORREO\",\"telefono\":\"0991234567\"}")"
ID=$(sed -n 's/.*"id":\([0-9]*\).*/\1/p' /tmp/smoke_body)
esperar "POST /clientes (sin teléfono)" 201 "$(http -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d "{\"nombres\":\"Sin\",\"apellidos\":\"Telefono\",\"correo\":\"sin.$CORREO\",\"telefono\":null}")"
esperar "POST /clientes correo duplicado" 409 "$(http -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d "{\"nombres\":\"Ana\",\"apellidos\":\"Prueba\",\"correo\":\"$CORREO\"}")"
esperar "POST /clientes inválido" 400 "$(http -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d '{"nombres":"","apellidos":"x","correo":"no-es-correo"}')"
esperar "GET /clientes" 200 "$(http "$BASE_URL/api/clientes" "${AUTH[@]}")"
esperar "GET /clientes/{id}" 200 "$(http "$BASE_URL/api/clientes/$ID" "${AUTH[@]}")"
esperar "PUT /clientes/{id}" 200 "$(http -X PUT "$BASE_URL/api/clientes/$ID" "${AUTH[@]}" -d "{\"nombres\":\"Ana María\",\"apellidos\":\"Prueba\",\"correo\":\"$CORREO\",\"telefono\":\"0991234567\"}")"
esperar "DELETE /clientes/{id}" 204 "$(http -X DELETE "$BASE_URL/api/clientes/$ID" "${AUTH[@]}")"
esperar "GET /clientes/{id} borrado" 404 "$(http "$BASE_URL/api/clientes/$ID" "${AUTH[@]}")"

echo
if [ "$fallos" -eq 0 ]; then echo "Todo en orden."; else echo "$fallos comprobación(es) fallaron."; exit 1; fi
