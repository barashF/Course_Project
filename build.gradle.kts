import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.example"
version = "1.0"

val javaVer = property("javaVersion") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVer))
    }
}

javafx {
    version = "17.0.2"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

application {
    mainClass.set("com.example.filesystemanalyzer.MainApp")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:17.0.2")
    implementation("org.openjfx:javafx-fxml:17.0.2")
    implementation("org.openjfx:javafx-swing:17.0.2")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    implementation("commons-io:commons-io:2.11.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(javaVer.toInt())
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED)
    }
}

tasks {
    named("distZip") {
        dependsOn("shadowJar")
    }
    named("distTar") {
        dependsOn("shadowJar")
    }
    named("startScripts") {
        dependsOn("shadowJar")
    }
    named("startShadowScripts") {
        dependsOn("jar")
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("FileSystemAnalyzer")
        archiveClassifier.set("")
        archiveVersion.set(version.toString())
        mergeServiceFiles()

        manifest {
            attributes["Main-Class"] = "com.example.filesystemanalyzer.MainApp"
            // Указываем модули JavaFX в аргументах JVM
            attributes["Add-Opens"] = "java.base/java.lang=ALL-UNNAMED java.base/java.nio=ALL-UNNAMED"
        }

        // Включение всех зависимостей, включая JavaFX
        configurations = listOf(project.configurations.runtimeClasspath.get())

        // Исключение конфликтующих файлов
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
        exclude("META-INF/versions/**", "META-INF/maven/**", "META-INF/LICENSE*")
        exclude("module-info.class")
    }
}