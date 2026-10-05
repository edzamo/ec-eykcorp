package com.eykcorp.clientes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de la arquitectura hexagonal (INV-01..03, INV-10, INV-12), definidas en
 * {@link ReglasArquitectura} y verificadas con canarios en {@link ReglasArquitecturaTest}.
 *
 * <p>Las reglas no admiten conjuntos vacíos (se retiró {@code allowEmptyShould}).
 * Excepción EXC-1 (aplicada por defecto, pendiente de confirmación del usuario): {@code reactor.core..} solo en {@code application}.
 */
@AnalyzeClasses(
        packages = "com.eykcorp.clientes",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String BASE = "com.eykcorp.clientes";

    @ArchTest
    static final ArchRule el_dominio_es_java_puro = ReglasArquitectura.elDominioEsJavaPuro(BASE);

    @ArchTest
    static final ArchRule la_aplicacion_no_depende_de_adaptadores_ni_frameworks =
            ReglasArquitectura.laAplicacionSoloDependeDeDominioYAbstracciones(BASE);

    @ArchTest
    static final ArchRule los_adaptadores_no_dependen_entre_si =
            ReglasArquitectura.losAdaptadoresNoDependenEntreSi(BASE);

    @ArchTest
    static final ArchRule sin_ciclos_entre_modulos = ReglasArquitectura.sinCiclosEntreModulos(BASE);

    @ArchTest
    static final ArchRule sin_llamadas_bloqueantes_de_reactor = ReglasArquitectura.sinLlamadasBloqueantes(BASE);

    @ArchTest
    static final ArchRule la_seguridad_no_depende_de_adaptadores =
            ReglasArquitectura.laSeguridadNoDependeDeAdaptadores(BASE);

    @ArchTest
    static final ArchRule los_puertos_tienen_como_maximo_siete_metodos =
            ReglasArquitectura.puertosConComoMaximoSieteMetodos(BASE);

    @ArchTest
    static final ArchRule los_modelos_de_infraestructura_no_reutilizan_nombres_de_dominio =
            ReglasArquitectura.modelosDeInfraNoReutilizanNombresDeDominio(BASE);

    @ArchTest
    static final ArchRule los_paquetes_de_puertos_solo_contienen_interfaces =
            ReglasArquitectura.losPuertosSoloContienenInterfaces(BASE);
}
