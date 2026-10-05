# MongoDB oficial + script que crea el usuario de la aplicación (readWrite solo sobre "auditoria").
# El script se COPIA a la imagen (no se monta desde el host) para que funcione igual en cualquier
# sistema: los montajes de archivos individuales fallan con EPERM en algunos Docker Desktop de macOS.
FROM mongo:7.0.43
COPY init-auditoria.js /docker-entrypoint-initdb.d/init-auditoria.js
