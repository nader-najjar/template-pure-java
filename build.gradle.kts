import com.github.spotbugs.snom.Confidence
import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

plugins {
    id("java")
    id("jacoco")
    id("checkstyle")
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
    toolVersion = "0.8.14"
}

checkstyle {
    toolVersion = "10.17.0"
    configFile = file("$projectDir/checkstyle-configuration.xml")
    isIgnoreFailures = false
}

spotbugs {
    toolVersion.set("4.9.8")
    effort.set(Effort.MAX)
    reportLevel.set(Confidence.LOW)
    excludeFilter.set(file("$projectDir/spotbugs-configuration.xml"))
}

repositories {
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
    lockMode = LockMode.STRICT
}

dependencies {
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.21")

    // Dependency Injection
    implementation("com.google.inject:guice:7.0.0")

    // JSON `serde`
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.20.1")

    // Validators
    implementation(platform("org.hibernate.validator:hibernate-validator-bom:9.1.0.Final"))
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.glassfish.expressly:expressly:6.0.0")

    // JUnit
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testRuntimeOnly("net.bytebuddy:byte-buddy-agent:1.15.4")

    // Spotbugs
    testCompileOnly("com.github.spotbugs:spotbugs-annotations:4.9.8")
}

/**
 * Gradle Compile Task Configurations
 */

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

/**
 * Gradle Javadoc Task Configurations
 */

tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        addBooleanOption("Xdoclint:all,-missing", true)
    }
}

/**
 * Gradle Clean Task Configurations
 */

tasks.named<Delete>("clean") {
    delete("bin")
}

/**
 * Gradle Test Task Configurations
 */

val mockitoAgentFiles = configurations.testRuntimeClasspath.map { cp ->
    cp.filter { it.name.contains("byte-buddy-agent") }
}

private class MockitoAgentArgumentProvider(
    @get:InputFiles
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val agentFiles: Provider<org.gradle.api.file.FileCollection>
) : CommandLineArgumentProvider {
    override fun asArguments(): Iterable<String> =
        agentFiles.get().firstOrNull()?.let { listOf("-javaagent:${it.absolutePath}") } ?: emptyList()
}

tasks.test {
    useJUnitPlatform()
    jvmArgumentProviders += MockitoAgentArgumentProvider(mockitoAgentFiles)
    reports { junitXml.required = true; html.required = true }
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
    }
}

/**
 * Gradle Check Task Configurations
 */

tasks.check {
    dependsOn(
        tasks.jacocoTestCoverageVerification,
        tasks.checkstyleMain,
        tasks.checkstyleTest,
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
 * Checkstyle Configurations
 */

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(true)
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
