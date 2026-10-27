package br.edu.ifsp.saocarlos.inscricoes;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "br.edu.ifsp.saocarlos.inscricoes",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    private static final String BASE = "br.edu.ifsp.saocarlos.inscricoes.";

    @ArchTest
    static final ArchRule camadasRespeitamAsDependencias = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Api").definedBy(BASE + "api..")
            .layer("Dominio").definedBy(BASE + "dominio..")
            .layer("Infra").definedBy(BASE + "infra..")
            .whereLayer("Api").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infra").mayNotBeAccessedByAnyLayer()
            .whereLayer("Dominio").mayOnlyBeAccessedByLayers("Api", "Infra");

    @ArchTest
    static final ArchRule dominioNaoDependeDeFramework = noClasses()
            .that().resideInAPackage(BASE + "dominio..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "io.quarkus..", "jakarta..", "javax..", "org.hibernate..",
                    "com.fasterxml..", "io.smallrye..");

    @ArchTest
    static final ArchRule apiNaoDependeDeInfra = noClasses()
            .that().resideInAPackage(BASE + "api..")
            .should().dependOnClassesThat().resideInAPackage(BASE + "infra..");
}
