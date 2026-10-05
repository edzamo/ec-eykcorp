package com.eykcorp.clientes;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Reglas de arquitectura parametrizadas por paquete base (reutilizables con fixtures). */
final class ReglasArquitectura {

    private static final int MAX_METODOS_POR_PUERTO = 7;

    /**
     * EXC-1 (aplicada por defecto, pendiente de confirmación del usuario): reactor.core solo en
     * application. Quitar esta constante si se rechaza. EXC-2: sin @Transactional en application
     * (ningún caso de uso lo usa; la regla lo rechaza). No se abre reactor.util.* completo (solo function, context y retry).
     */
    private static final String REACTOR_EXC_1 = "reactor.core..";

    private ReglasArquitectura() {
    }

    static ArchRule elDominioEsJavaPuro(String base) {
        return classes()
                .that().resideInAPackage(base + ".domain..")
                .should().onlyDependOnClassesThat(resideInAnyPackage(
                        "java..",
                        base + ".domain.."));
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
                                "reactor.util.function..",
                                "reactor.util.context..",
                                "reactor.util.retry..")
                                .or(equivalentTo(Service.class)));
    }

    static ArchRule losAdaptadoresNoDependenEntreSi(String base) {
        return slices()
                .matching(base + ".infrastructure.adapter.(*).(*)..")
                .should().notDependOnEachOther();
    }

    static ArchRule sinCiclosEntreModulos(String base) {
        return slices()
                .matching(base + ".(*)..")
                .should().beFreeOfCycles();
    }

    /** Reactivo de punta a punta: ninguna llamada bloqueante de Reactor en producción. */
    static ArchRule sinLlamadasBloqueantes(String base) {
        return noClasses()
                .that().resideInAPackage(base + "..")
                .should().callMethodWhere(llamadaBloqueante())
                .because("el código reactivo nunca bloquea el event loop");
    }

    private static DescribedPredicate<JavaMethodCall> llamadaBloqueante() {
        Set<String> prohibidas = Set.of(
                "reactor.core.publisher.Mono.block",
                "reactor.core.publisher.Mono.blockOptional",
                "reactor.core.publisher.Flux.blockFirst",
                "reactor.core.publisher.Flux.blockLast",
                "reactor.core.publisher.Flux.toIterable",
                "reactor.core.publisher.Flux.toStream");
        return DescribedPredicate.describe(
                "bloquea (Mono.block*, Flux.blockFirst/blockLast/toIterable/toStream)",
                llamada -> prohibidas.contains(
                        llamada.getTargetOwner().getName() + "." + llamada.getName()));
    }

    static ArchRule laSeguridadNoDependeDeAdaptadores(String base) {
        return noClasses()
                .that().resideInAPackage(base + ".infrastructure.security..")
                .should().dependOnClassesThat().resideInAPackage(base + ".infrastructure.adapter..");
    }

    /** INV-16: los modelos de persistencia/auditoría no comparten nombre simple con el dominio. */
    static ArchRule modelosDeInfraNoReutilizanNombresDeDominio(String base) {
        return classes().should(new ArchCondition<JavaClass>("no reutilizar nombres simples del dominio") {
            private Set<String> nombresDeDominio;

            @Override
            public void init(Collection<JavaClass> todas) {
                nombresDeDominio = todas.stream()
                        .filter(c -> c.getPackageName().startsWith(base + ".domain"))
                        .map(JavaClass::getSimpleName)
                        .collect(Collectors.toSet());
            }

            @Override
            public void check(JavaClass clase, ConditionEvents events) {
                boolean enInfra = resideInAnyPackage(
                                base + ".infrastructure.adapter.out.persistence..",
                                base + ".infrastructure.adapter.out.audit..")
                        .test(clase);
                if (enInfra && nombresDeDominio.contains(clase.getSimpleName())) {
                    events.add(SimpleConditionEvent.violated(
                            clase, clase.getName() + " repite el nombre de una clase del dominio"));
                }
            }
        });
    }

    static ArchRule puertosConComoMaximoSieteMetodos(String base) {
        return classes()
                .that().resideInAPackage(base + ".application.port..")
                .and().areInterfaces()
                .should(new ArchCondition<JavaClass>("declarar como máximo " + MAX_METODOS_POR_PUERTO + " métodos") {
                    @Override
                    public void check(JavaClass puerto, ConditionEvents events) {
                        int metodos = puerto.getMethods().size();
                        if (metodos > MAX_METODOS_POR_PUERTO) {
                            events.add(SimpleConditionEvent.violated(
                                    puerto, puerto.getName() + " declara " + metodos + " métodos"));
                        }
                    }
                });
    }
}
