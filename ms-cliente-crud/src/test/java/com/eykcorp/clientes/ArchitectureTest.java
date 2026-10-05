package com.eykcorp.clientes;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

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
    static final ArchRule el_dominio_es_java_puro = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat(resideInAnyPackage(
                    BASE + ".application..",
                    BASE + ".infrastructure..",
                    "org.springframework..",
                    "jakarta..",
                    "com.fasterxml..",
                    "lombok..",
                    "org.slf4j..",
                    "reactor..",
                    "org.reactivestreams.."))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule la_aplicacion_no_depende_de_adaptadores_ni_frameworks = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat(
                    resideInAPackage(BASE + ".infrastructure..")
                            .or(resideInAnyPackage(
                                    "jakarta..",
                                    "com.fasterxml..",
                                    "lombok..",
                                    "org.slf4j..",
                                    "reactor.netty..",
                                    "org.springframework.web..",
                                    "org.springframework.data.."))
                            .or(resideInAPackage("org.springframework..")
                                    .and(not(equivalentTo(Service.class)))))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule los_adaptadores_no_dependen_entre_si = slices()
            .matching(BASE + ".infrastructure.adapter.(*)..")
            .should().notDependOnEachOther()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule sin_ciclos_entre_modulos = slices()
            .matching(BASE + ".(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);
}
