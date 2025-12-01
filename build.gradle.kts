import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions

plugins {
    id("java")
    id("jacoco")
    id("com.github.spotbugs") version "6.0.26"
}

group = "io.template"
version = "1.0-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(24)) }
    withSourcesJar()
    withJavadocJar()
}

jacoco {
    toolVersion = "0.8.12"
}

spotbugs {
    toolVersion.set("4.9.8")
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.HIGH)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.21")

    // TODO: Replace this with micronaut
    implementation("com.google.inject:guice:7.0.0")

    // TODO: Replace this with micronaut
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")

    implementation(platform("org.hibernate.validator:hibernate-validator-bom:9.1.0.Final"))
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.glassfish.expressly:expressly:6.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // TODO checkstyle and spotbugs
}

/**
 * Gradle Task Configurations
 */

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        addBooleanOption("Xdoclint:all,-missing", true)
    }
}

tasks.named<Delete>("clean") {
    delete("bin")
}

tasks.test {
    useJUnitPlatform()
    reports { junitXml.required = true; html.required = true }
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
    }
}

tasks.check {
    dependsOn(
        tasks.jacocoTestCoverageVerification,
        tasks.spotbugsMain,
        tasks.spotbugsTest
    )
}

/**
 * Jacaco Configurations
 */

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

/**
 * Spotbugs Configurations
 */

tasks.withType<SpotBugsTask>().configureEach {
    reports.maybeCreate("xml").required.set(true)
    reports.maybeCreate("html").required.set(true)
}

tasks.spotbugsMain {
    dependsOn(tasks.classes)
}

tasks.spotbugsTest {
    dependsOn(tasks.testClasses)
}
