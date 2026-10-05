#!/usr/bin/env bash
# Se ejecuta automáticamente cuando LocalStack queda listo (carpeta ready.d).
# Aprovisiona los recursos AWS simulados que usa el proyecto. Usa `awslocal`,
# que ya apunta al emulador local; no se necesita ninguna cuenta AWS real.
set -euo pipefail

echo "[localstack-init] creando recursos…"

# S3: bucket de hosting estático para la SPA (ms-cliente-presentacion)
awslocal s3 mb s3://eykcorp-clientes-web
awslocal s3 website s3://eykcorp-clientes-web --index-document index.html --error-document index.html

# SQS: cola para el envío asíncrono de la auditoría de clientes
awslocal sqs create-queue --queue-name auditoria-clientes

# Secrets Manager: secreto JWT del backend (en producción real se leería de aquí)
awslocal secretsmanager create-secret \
  --name eykcorp/clientes/jwt-secret \
  --secret-string "${JWT_SECRET:-solo-para-pruebas-locales-0123456789abcdef}"

echo "[localstack-init] listo: s3://eykcorp-clientes-web, cola auditoria-clientes, secreto eykcorp/clientes/jwt-secret"
