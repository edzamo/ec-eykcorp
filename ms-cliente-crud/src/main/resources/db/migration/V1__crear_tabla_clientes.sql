CREATE TABLE clientes (
    id             BIGSERIAL    PRIMARY KEY,
    nombres        VARCHAR(100) NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    correo         VARCHAR(254) NOT NULL,
    telefono       VARCHAR(20),
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_clientes_correo UNIQUE (correo)
);
