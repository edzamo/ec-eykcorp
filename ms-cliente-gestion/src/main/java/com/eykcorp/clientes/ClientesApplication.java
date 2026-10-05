package com.eykcorp.clientes;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Composition root: el escaneo de componentes cablea casos de uso y adaptadores. */
@SpringBootApplication
public class ClientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientesApplication.class, args);
    }

    @Bean
    Clock reloj() {
        return Clock.systemUTC();
    }
}
