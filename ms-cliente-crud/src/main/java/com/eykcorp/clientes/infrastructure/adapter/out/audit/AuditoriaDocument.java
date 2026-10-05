package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Modelo de persistencia de la auditoría. Minimización: sin correo ni otros datos personales. */
@Document("auditoria")
record AuditoriaDocument(@Id String id, String accion, Long clienteId, Instant timestamp) {
}
