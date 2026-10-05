package com.eykcorp.clientes.infrastructure.adapter.out.audit;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.MongoDBContainer;

/** MongoDB compartido (singleton) para las pruebas de integración; misma versión que docker-compose. */
public final class MongoTestContainer {

    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0.43");

    static {
        MONGO.start();
    }

    private MongoTestContainer() {
    }

    public static void registrarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> MONGO.getReplicaSetUrl("auditoria"));
    }
}
