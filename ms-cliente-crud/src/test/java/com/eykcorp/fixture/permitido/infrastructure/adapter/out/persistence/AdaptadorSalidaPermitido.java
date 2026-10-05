package com.eykcorp.fixture.permitido.infrastructure.adapter.out.persistence;

import com.eykcorp.fixture.permitido.application.port.out.PuertoPermitido;
import com.eykcorp.fixture.permitido.domain.ModeloPermitido;

public abstract class AdaptadorSalidaPermitido implements PuertoPermitido {
    ModeloPermitido modelo;
}
