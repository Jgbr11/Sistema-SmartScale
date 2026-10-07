package br.com.milscale.milscale;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.milscale", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioNaoConheceAplicacaoNemAdaptadores = noClasses()
            .that().resideInAPackage("br.com.milscale.milscale.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "br.com.milscale.milscale.application..", "br.com.milscale.milscale.adapters..", "org.springframework..");

    @ArchTest
    static final ArchRule aplicacaoNaoConheceWebNemServlet = noClasses()
            .that().resideInAPackage("br.com.milscale.milscale.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "br.com.milscale.milscale.adapters.web..", "br.com.milscale.milscale.adapters.config..",
                    "org.springframework.web..", "jakarta.servlet..");

    @ArchTest
    static final ArchRule aplicacaoNaoConheceInfraestruturaDeEmail = noClasses()
            .that().resideInAPackage("br.com.milscale.milscale.application..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.mail..");

    @ArchTest
    static final ArchRule controllersSoNoAdaptadorWeb = classes()
            .that().areAnnotatedWith(RestController.class)
            .should().resideInAPackage("br.com.milscale.milscale.adapters.web..");
}
