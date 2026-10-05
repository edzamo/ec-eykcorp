#!/usr/bin/env bash
# Despliega la SPA compilada a un bucket S3 SIMULADO (LocalStack), como si fuera AWS.
# Requisitos: el stack con el perfil aws levantado (docker compose --profile aws up -d localstack)
set -euo pipefail
RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
RED="${COMPOSE_NETWORK:-eykcorp-clientes_eyk-net}"

echo "1) Compilando la SPA…"
(cd "$RAIZ/ms-cliente-presentacion" && npm ci --silent && npm run build --silent)

echo "2) Sincronizando dist/ con s3://eykcorp-clientes-web (LocalStack)…"
docker run --rm --network "$RED" \
  -e AWS_ACCESS_KEY_ID=test -e AWS_SECRET_ACCESS_KEY=test -e AWS_DEFAULT_REGION=us-east-1 \
  -v "$RAIZ/ms-cliente-presentacion/dist:/dist:ro" \
  amazon/aws-cli:2.27.50 --endpoint-url http://localstack:4566 \
  s3 sync /dist s3://eykcorp-clientes-web --delete

echo "3) Contenido del bucket:"
docker run --rm --network "$RED" \
  -e AWS_ACCESS_KEY_ID=test -e AWS_SECRET_ACCESS_KEY=test -e AWS_DEFAULT_REGION=us-east-1 \
  amazon/aws-cli:2.27.50 --endpoint-url http://localstack:4566 s3 ls s3://eykcorp-clientes-web --recursive

echo "Sitio simulado: http://localhost:4566/eykcorp-clientes-web/index.html"
