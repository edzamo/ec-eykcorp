package com.eykcorp.clientes;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

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
    void debe_detectar_aplicacion_que_depende_de_reactor_util() {
        String base = FIXTURE + ".violacion.aplicacion";
        debeDetectar(
                ReglasArquitectura.laAplicacionNoDependeDeAdaptadoresNiFrameworks(base), importar(base));
    }

    @Test
    void debe_permitir_aplicacion_con_service_y_reactor_core() {
        String base = FIXTURE + ".permitido";
        debePermitir(
                ReglasArquitectura.laAplicacionNoDependeDeAdaptadoresNiFrameworks(base), importar(base));
    }

    @Test
    void debe_ignorar_paquetes_domain_y_application_ajenos_al_base() {
        JavaClasses ajenas = importar(FIXTURE + ".ajeno");
        debePermitir(ReglasArquitectura.elDominioEsJavaPuro(BASE_REAL), ajenas);
        debePermitir(ReglasArquitectura.laAplicacionNoDependeDeAdaptadoresNiFrameworks(BASE_REAL), ajenas);
    }
}
