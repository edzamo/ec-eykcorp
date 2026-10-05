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
  if [ "$2" = "$3" ]; then ok "$1 -> $3"; else fallo "$1 (esperado $2, obtenido $3)"; sed 's/^/        respuesta: /' /tmp/smoke_body | head -c 400; echo; fi
}
http() { curl -s -o /tmp/smoke_body -w '%{http_code}' "$@"; }
# probar descripción código_esperado <argumentos de curl>  (evita comillas anidadas dentro de $(...), que bash 3.2 de macOS interpreta mal)
probar() { local desc="$1" esperado="$2" real; shift 2; real=$(http "$@"); esperar "$desc" "$esperado" "$real"; }

echo "Frontend y proxy"
probar "GET / (SPA)" 200 "$BASE_URL/"
probar "GET /api/actuator/health" 200 "$BASE_URL/api/actuator/health"

echo "Seguridad"
probar "GET /api/clientes sin token" 401 "$BASE_URL/api/clientes"
probar "Login con contraseña incorrecta" 401 -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' -d "{\"usuario\":\"$ADMIN_USER\",\"password\":\"incorrecta\"}"
codigo=$(http -X POST "$BASE_URL/api/auth/login" -H 'Content-Type: application/json' -d "{\"usuario\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASSWORD\"}")
esperar "Login correcto" 200 "$codigo"
TOKEN=$(sed -n 's/.*"token":"\([^"]*\)".*/\1/p' /tmp/smoke_body)
[ -n "$TOKEN" ] || { fallo "no se obtuvo token"; exit 1; }
AUTH=(-H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json')

echo "CRUD de clientes"
CORREO="smoke.$(date +%s)@example.com"
probar "POST /clientes (con teléfono)" 201 -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d "{\"nombres\":\"Ana\",\"apellidos\":\"Prueba\",\"correo\":\"$CORREO\",\"telefono\":\"0991234567\"}"
ID=$(sed -n 's/.*"id":\([0-9]*\).*/\1/p' /tmp/smoke_body)
probar "POST /clientes (sin teléfono)" 201 -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d "{\"nombres\":\"Sin\",\"apellidos\":\"Telefono\",\"correo\":\"sin.$CORREO\",\"telefono\":null}"
probar "POST /clientes correo duplicado" 409 -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d "{\"nombres\":\"Ana\",\"apellidos\":\"Prueba\",\"correo\":\"$CORREO\"}"
probar "POST /clientes inválido" 400 -X POST "$BASE_URL/api/clientes" "${AUTH[@]}" -d '{"nombres":"","apellidos":"x","correo":"no-es-correo"}'
probar "GET /clientes" 200 "$BASE_URL/api/clientes" "${AUTH[@]}"
probar "GET /clientes/{id}" 200 "$BASE_URL/api/clientes/$ID" "${AUTH[@]}"
probar "PUT /clientes/{id}" 200 -X PUT "$BASE_URL/api/clientes/$ID" "${AUTH[@]}" -d "{\"nombres\":\"Ana María\",\"apellidos\":\"Prueba\",\"correo\":\"$CORREO\",\"telefono\":\"0991234567\"}"
probar "DELETE /clientes/{id}" 204 -X DELETE "$BASE_URL/api/clientes/$ID" "${AUTH[@]}"
probar "GET /clientes/{id} borrado" 404 "$BASE_URL/api/clientes/$ID" "${AUTH[@]}"

echo
if [ "$fallos" -eq 0 ]; then echo "Todo en orden."; else echo "$fallos comprobación(es) fallaron."; exit 1; fi
