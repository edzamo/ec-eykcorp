package com.eykcorp.clientes;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eykcorp.clientes.domain.cliente.Cliente;
import com.eykcorp.clientes.application.service.ClienteService;
import com.eykcorp.fixture.ajeno.application.ServicioAjenoAlBase;
import com.eykcorp.fixture.ajeno.domain.ClaseAjenaAlBase;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Canarios: verifican que cada regla detecta su violación (fixtures bajo
 * {@code com.eykcorp.fixture}, solo en src/test) y no rechaza lo permitido.
 */
class ReglasArquitecturaTest {

    private static final String FIXTURE = "com.eykcorp.fixture";
    private static final String BASE_REAL = "com.eykcorp.clientes";

    private static JavaClasses importar(String paquete) {
        return new ClassFileImporter().importPackages(paquete);
    }

    private static void debeDetectar(ArchRule regla, JavaClasses clases) {
        assertThatThrownBy(() -> regla.check(clases)).isInstanceOf(AssertionError.class);
    }

    private static void debePermitir(ArchRule regla, JavaClasses clases) {
        assertThatCode(() -> regla.check(clases)).doesNotThrowAnyException();
    }

    @Test
    void debe_detectar_dependencia_entre_adaptadores_de_salida_distintos() {
        String base = FIXTURE + ".violacion.outout";
        debeDetectar(ReglasArquitectura.losAdaptadoresNoDependenEntreSi(base), importar(base));
    }

    @Test
    void debe_detectar_dependencia_entre_adaptadores_de_entrada_distintos() {
        String base = FIXTURE + ".violacion.inin";
        debeDetectar(ReglasArquitectura.losAdaptadoresNoDependenEntreSi(base), importar(base));
    }

    @Test
    void debe_detectar_dominio_que_depende_de_un_framework_fuera_de_la_lista_blanca() {
        String base = FIXTURE + ".violacion.dominio";
        debeDetectar(ReglasArquitectura.elDominioEsJavaPuro(base), importar(base));
    }

    @Test
    void debe_detectar_dominio_que_usa_lombok() {
        String base = FIXTURE + ".violacion.lombok";
        debeDetectar(ReglasArquitectura.elDominioEsJavaPuro(base), importar(base));
    }

    @Test
    void debe_detectar_aplicacion_que_depende_de_reactor_util() {
        String base = FIXTURE + ".violacion.aplicacion";
        debeDetectar(
                ReglasArquitectura.laAplicacionSoloDependeDeDominioYAbstracciones(base), importar(base));
    }

    @Test
    void debe_permitir_aplicacion_con_service_y_reactor_core() {
        String base = FIXTURE + ".permitido";
        debePermitir(
                ReglasArquitectura.laAplicacionSoloDependeDeDominioYAbstracciones(base), importar(base));
    }

    @Test
    void debe_ignorar_paquetes_domain_y_application_ajenos_al_base() {
        JavaClasses ajenas = importar(FIXTURE + ".ajeno");
        // junto al dominio real (la regla ya no admite conjuntos vacíos)
        debePermitir(
                ReglasArquitectura.elDominioEsJavaPuro(BASE_REAL),
                new ClassFileImporter().importClasses(ClaseAjenaAlBase.class, Cliente.class));
        debePermitir(
                ReglasArquitectura.laAplicacionSoloDependeDeDominioYAbstracciones(BASE_REAL),
                new ClassFileImporter().importClasses(ServicioAjenoAlBase.class, ClienteService.class));
    }

    @Test
    void debe_permitir_aplicacion_con_mono_zip_y_transactional() {
        String base = FIXTURE + ".permitido";
        debePermitir(
                ReglasArquitectura.laAplicacionSoloDependeDeDominioYAbstracciones(base), importar(base));
    }

    @ParameterizedTest
    @ValueSource(strings = {"tx", "slf4j", "validation", "data", "lombok"})
    void debe_detectar_aplicacion_que_depende_de_infraestructura_prohibida(String escenario) {
        String base = FIXTURE + ".violacion." + escenario;
        debeDetectar(
                ReglasArquitectura.laAplicacionSoloDependeDeDominioYAbstracciones(base), importar(base));
    }

    @Test
    void debe_detectar_ciclo_entre_domain_y_application() {
        String base = FIXTURE + ".violacion.ciclo";
        debeDetectar(ReglasArquitectura.sinCiclosEntreModulos(base), importar(base));
    }

    @Test
    void debe_permitir_adaptadores_que_dependen_de_application_y_domain() {
        String base = FIXTURE + ".permitido";
        debePermitir(ReglasArquitectura.losAdaptadoresNoDependenEntreSi(base), importar(base));
        debePermitir(ReglasArquitectura.sinCiclosEntreModulos(base), importar(base));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Mono.block()", "Mono.blockOptional()", "Flux.blockFirst()", "Flux.blockLast()",
        "Flux.toIterable()", "Flux.toStream()"})
    void debe_detectar_llamadas_bloqueantes_de_reactor(String llamada) {
        String base = FIXTURE + ".violacion.bloqueo";
        assertThatThrownBy(() -> ReglasArquitectura.sinLlamadasBloqueantes(base).check(importar(base)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining(llamada);
    }

    @Test
    void debe_permitir_codigo_sin_llamadas_bloqueantes() {
        String base = FIXTURE + ".permitido";
        debePermitir(ReglasArquitectura.sinLlamadasBloqueantes(base), importar(base));
    }

    @Test
    void debe_detectar_seguridad_que_depende_de_adaptadores() {
        String base = FIXTURE + ".violacion.seguridad";
        debeDetectar(ReglasArquitectura.laSeguridadNoDependeDeAdaptadores(base), importar(base));
    }

    @Test
    void debe_permitir_seguridad_que_solo_depende_del_dominio() {
        String base = FIXTURE + ".permitido";
        debePermitir(ReglasArquitectura.laSeguridadNoDependeDeAdaptadores(base), importar(base));
    }

    @Test
    void debe_detectar_modelo_de_persistencia_o_auditoria_con_nombre_de_dominio() {
        String base = FIXTURE + ".violacion.nombres";
        assertThatThrownBy(() -> ReglasArquitectura.modelosDeInfraNoReutilizanNombresDeDominio(base)
                        .check(importar(base)))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("out.persistence.Cliente")
                .hasMessageContaining("out.audit.Cliente");
    }

    @Test
    void debe_permitir_modelos_de_infra_con_nombre_distinto_al_de_dominio() {
        String base = FIXTURE + ".permitido";
        debePermitir(ReglasArquitectura.modelosDeInfraNoReutilizanNombresDeDominio(base), importar(base));
    }

    @Test
    void debe_detectar_puerto_con_mas_de_siete_metodos() {
        String base = FIXTURE + ".violacion.puerto";
        debeDetectar(ReglasArquitectura.puertosConComoMaximoSieteMetodos(base), importar(base));
    }

    @Test
    void debe_permitir_puerto_con_pocos_metodos() {
        String base = FIXTURE + ".permitido";
        debePermitir(ReglasArquitectura.puertosConComoMaximoSieteMetodos(base), importar(base));
    }
}
