package com.furkan.ecommerce.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
    @Test
    void internal_packages_should_not_be_accessed_cross_module() {
        var imported = new ClassFileImporter().importPackages("com.furkan.ecommerce");
        ArchRuleDefinition.noClasses()
                .that().resideInAPackage("..cart..").and().haveNameNotMatching(".*Test")
                .should().dependOnClassesThat().resideInAPackage("..product.internal..").check(imported);
    }
}
