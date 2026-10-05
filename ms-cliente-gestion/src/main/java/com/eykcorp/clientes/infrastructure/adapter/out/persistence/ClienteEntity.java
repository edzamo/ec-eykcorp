package com.eykcorp.clientes.infrastructure.adapter.out.persistence;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** Modelo de persistencia (nombre distinto del dominio, INV-16). */
@Table("clientes")
@Getter
@Setter
public class ClienteEntity {

    @Id
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    @Column("fecha_creacion")
    private Instant fechaCreacion;
}
