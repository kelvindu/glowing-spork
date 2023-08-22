import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin ("jvm") version "1.9.0"
  application
  id("com.github.johnrengelman.shadow") version "7.1.2"
  kotlin("plugin.serialization") version "1.9.0"
}

group = "du.kelvin.minigames"
version = "0.0.2-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.4.4"
val junitJupiterVersion = "5.9.1"

val mainVerticleName = "du.kelvin.minigames.persistor.MainVerticle"
val appClassName = "du.kelvin.minigames.persistor.AppKt"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
//  mainClass.set(launcherClassName)
  mainClass.set(appClassName)
}

dependencies {
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-config")
  implementation("io.vertx:vertx-pg-client")
  implementation("io.vertx:vertx-lang-kotlin-coroutines")
  implementation("io.vertx:vertx-lang-kotlin")

  implementation("io.netty:netty-all:4.1.96.Final")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0-RC")
  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

  implementation("com.azure:azure-messaging-servicebus:7.13.3")

  implementation(kotlin("stdlib-jdk8"))
  implementation("com.ongres.scram:client:2.1")
  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Jar> {
  archiveClassifier.set("app")
  manifest {
    attributes(mapOf("Main-Class" to appClassName))
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
