package com.eykcorp.clientes.application.port.out;

class InMemoryClienteRepositoryTest extends ClienteRepositoryPortContract {

    private final InMemoryClienteRepository repo = new InMemoryClienteRepository();

    @Override
    protected ClienteRepositoryPort repositorio() {
        return repo;
    }
}
