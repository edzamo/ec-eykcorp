package com.eykcorp.fixture.permitido.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

/** EXC-3: Lombok y SLF4J están permitidos en application (no en domain). */
@Getter
@RequiredArgsConstructor
public class ServicioConLombokYSlf4j {
    private final String dato;
    Logger log;
}
