package com.eykcorp.clientes;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

/** Reglas de arquitectura parametrizadas por paquete base (reutilizables con fixtures). */
final class ReglasArquitectura {

    /** EXC-1 (pendiente de confirmación del usuario): reactor.core solo en application. Quitar esta constante si se rechaza. */
    private static final String REACTOR_EXC_1 = "reactor.core..";

    private ReglasArquitectura() {
    }

    static ArchRule elDominioEsJavaPuro(String base) {
        return classes()
                .that().resideInAPackage(base + ".domain..")
                .should().onlyDependOnClassesThat(resideInAnyPackage(
                        "java..",
                        base + ".domain.."))
                .allowEmptyShould(true);
    }

    static ArchRule laAplicacionSoloDependeDeDominioYAbstracciones(String base) {
        return classes()
                .that().resideInAPackage(base + ".application..")
                .should().onlyDependOnClassesThat(
                        resideInAnyPackage(
                                "java..",
                                base + ".domain..",
                                base + ".application..",
                                REACTOR_EXC_1,
                                "org.springframework.transaction.annotation..")
                                .or(equivalentTo(Service.class)))
                .allowEmptyShould(true);
    }

    static ArchRule losAdaptadoresNoDependenEntreSi(String base) {
        return slices()
                .matching(base + ".infrastructure.adapter.(*).(*)..")
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
