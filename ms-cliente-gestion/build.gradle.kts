plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.eykcorp"
version = "0.1.0"
description = "Microservicio de gestión (CRUD) de clientes"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

// SEC-001: Spring Boot 3.5.16 deja transitivos por debajo de los parches de seguridad; se fijan
// versiones exactas (retirar cuando el BOM de Boot ya las incluya).
ext["netty.version"] = "4.1.137.Final"        // CVE netty-codec-http < 4.1.137
ext["jackson-bom.version"] = "2.21.7"         // CVE jackson-databind < 2.21.7
ext["postgresql.version"] = "42.7.12"         // CVE org.postgresql:postgresql < 42.7.12

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.postgresql:r2dbc-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    // Swagger UI sirviendo el contrato YAML (contract-first): la generación desde el código está desactivada
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.17")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    // Flyway migra por JDBC solo al arrancar; el acceso a datos en runtime es R2DBC
    runtimeOnly("org.springframework:spring-jdbc")
    runtimeOnly("org.postgresql:postgresql")

    // Lombok: solo en infrastructure (INV-12 / ARCH-011; lo vigila ReglasArquitectura)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    // Pruebas de conformidad del contrato OpenAPI
    testImplementation("io.swagger.parser.v3:swagger-parser:2.1.48")
    testImplementation("com.atlassian.oai:swagger-request-validator-core:2.46.1")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

// CR-004: umbral de cobertura a nivel de bundle. Lombok queda excluido por lombok.config
// (@lombok.Generated) y se excluye explícitamente el arranque (ClientesApplication).
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) { exclude("**/ClientesApplication*") }
    }))
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)
}
