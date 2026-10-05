package com.eykcorp.clientes.application.port.out;

import com.eykcorp.clientes.application.port.out.FakeAuditoriaPort.Registro;
import java.util.List;

/** Caracterización del fake frente al contrato (no cuenta como RED: el fake ya existía). */
class FakeAuditoriaPortTest extends AuditoriaPortContract {

    private final FakeAuditoriaPort fake = new FakeAuditoriaPort();

    @Override
    protected AuditoriaPort puerto() {
        return fake;
    }

    @Override
    protected List<Registro> registrosDe(Long clienteId) {
        return fake.registros().stream().filter(r -> r.clienteId().equals(clienteId)).toList();
    }
}
