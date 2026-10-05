package com.eykcorp.clientes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de la arquitectura hexagonal (INV-01..03, INV-10, INV-12).
 *
 * <p>En E0 los paquetes están vacíos, por lo que estas reglas son vacuosas
 * ({@code allowEmptyShould}) hasta que E1 introduzca clases. Excepción
 * EXC-1: {@code reactor.core..} se permite únicamente en {@code application}.
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
            ReglasArquitectura.laAplicacionNoDependeDeAdaptadoresNiFrameworks(BASE);

    @ArchTest
    static final ArchRule los_adaptadores_no_dependen_entre_si =
            ReglasArquitectura.losAdaptadoresNoDependenEntreSi(BASE);

    @ArchTest
    static final ArchRule sin_ciclos_entre_modulos = ReglasArquitectura.sinCiclosEntreModulos(BASE);
}
