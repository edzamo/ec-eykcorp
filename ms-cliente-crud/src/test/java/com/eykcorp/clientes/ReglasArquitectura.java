package com.eykcorp.clientes;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

/** Reglas de arquitectura parametrizadas por paquete base (reutilizables con fixtures). */
final class ReglasArquitectura {

    private ReglasArquitectura() {
    }

    static ArchRule elDominioEsJavaPuro(String base) {
        return noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat(resideInAnyPackage(
                        base + ".application..",
                        base + ".infrastructure..",
                        "org.springframework..",
                        "jakarta..",
                        "com.fasterxml..",
                        "lombok..",
                        "org.slf4j..",
                        "reactor..",
                        "org.reactivestreams.."))
                .allowEmptyShould(true);
    }

    static ArchRule laAplicacionNoDependeDeAdaptadoresNiFrameworks(String base) {
        return noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat(
                        resideInAPackage(base + ".infrastructure..")
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
    }

    static ArchRule losAdaptadoresNoDependenEntreSi(String base) {
        return slices()
                .matching(base + ".infrastructure.adapter.(*)..")
                .should().notDependOnEachOther()
                .allowEmptyShould(true);
    }

    static ArchRule sinCiclosEntreModulos(String base) {
        return slices()
                .matching(base + ".(*)..")
                .should().beFreeOfCycles()
                .allowEmptyShould(true);
    }
}
