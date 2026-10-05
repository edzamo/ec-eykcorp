package com.eykcorp.clientes.infrastructure.adapter.in.web;

import com.eykcorp.clientes.application.port.in.ActualizarClienteUseCase;
import com.eykcorp.clientes.application.port.in.CrearClienteUseCase;
import com.eykcorp.clientes.application.port.in.EliminarClienteUseCase;
import com.eykcorp.clientes.application.port.in.ListarClientesUseCase;
import com.eykcorp.clientes.application.port.in.ObtenerClienteUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final CrearClienteUseCase crear;
    private final ListarClientesUseCase listar;
    private final ObtenerClienteUseCase obtener;
    private final ActualizarClienteUseCase actualizar;
    private final EliminarClienteUseCase eliminar;
    private final ClienteWebMapper mapper;

    @PostMapping
    public Mono<ResponseEntity<ClienteResponse>> crear(@Valid @RequestBody ClienteRequest peticion) {
        return crear.crear(mapper.aDatos(peticion))
                .map(mapper::aRespuesta)
                .map(r -> ResponseEntity.created(URI.create("/clientes/" + r.id())).body(r));
    }

    @GetMapping
    public Flux<ClienteResponse> listar() {
        return listar.listar().map(mapper::aRespuesta);
    }

    @GetMapping("/{id}")
    public Mono<ClienteResponse> obtener(@PathVariable Long id) {
        return obtener.obtener(id).map(mapper::aRespuesta);
    }

    @PutMapping("/{id}")
    public Mono<ClienteResponse> actualizar(
            @PathVariable Long id, @Valid @RequestBody ClienteRequest peticion) {
        return actualizar.actualizar(id, mapper.aDatos(peticion)).map(mapper::aRespuesta);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> eliminar(@PathVariable Long id) {
        return eliminar.eliminar(id);
    }
}
