// Se ejecuta SOLO la primera vez que arranca MongoDB con el volumen vacío.
// Crea un usuario de aplicación con permisos mínimos (readWrite solo sobre "auditoria"),
// para que el backend no use el usuario root.
const usuario = process.env.MONGO_APP_USER;
const clave = process.env.MONGO_APP_PASSWORD;
if (!usuario || !clave) {
  throw new Error("Defina MONGO_APP_USER y MONGO_APP_PASSWORD");
}
const auditoria = db.getSiblingDB("auditoria");
auditoria.createUser({ user: usuario, pwd: clave, roles: [{ role: "readWrite", db: "auditoria" }] });
