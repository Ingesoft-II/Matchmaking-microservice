package com.killerprotocol.matchmaking.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

@AnalyzeClasses(packages = "com.killerprotocol.matchmaking")
class HexagonalArchitectureTest {

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule elDominioNoDependeDeOtrasCapasNiDeSpring =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..adapter..", "org.springframework..",
                            "org.springframework.data.redis..", "org.springframework.amqp..",
                            "org.springframework.amqp..", "redis..", "amqp..");

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule laAplicacionNoDependeDeAdaptadoresNiDeSpring =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..adapter..", "org.springframework..");

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule losAdaptadoresInNoDependenDeLosOut =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter.out..");

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule losAdaptadoresOutNoDependenDeLosIn =
            noClasses().that().resideInAPackage("..adapter.out..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter.in..");

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule losServiciosNoTienenAnotacionesDeSpring =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().notBeAnnotatedWith("org.springframework.stereotype.Service")
                    .andShould().notBeAnnotatedWith("org.springframework.stereotype.Component")
                    .andShould().notBeAnnotatedWith("org.springframework.stereotype.Controller")
                    .andShould().notBeAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                    .allowEmptyShould(true);
}
