/*
 * Copyright (c) 2021 mrvanish97 [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import java.util.Properties

plugins {
  id("org.springframework.boot") version "2.5.2"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version "1.5.10"
  kotlin("plugin.spring") version "1.5.10"
  signing
  `maven-publish`
}

group = "io.github.mrvanish97.kbnsext"
version = "0.3.2"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot")

  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation("org.jetbrains.kotlin:kotlin-script-util")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm")

  implementation("net.bytebuddy:byte-buddy:1.11.6")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
  secretPropsFile.reader().use {
    Properties().apply {
      load(it)
    }
  }.onEach { (name, value) ->
    ext[name.toString()] = value
  }
} else {
  ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
  ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
  ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE")
  ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
  ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val sourcesJar by tasks.registering(Jar::class) {
  archiveClassifier.set("sources")
  from(sourceSets.main.get().allSource)
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {
// Configure maven central repository
  repositories {
    maven {
      name = "sonatype"
      setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
      credentials {
        username = getExtraString("ossrhUsername")
        password = getExtraString("ossrhPassword")
      }
    }
  }

// Configure all publications
  publications {

    create<MavenPublication>("mavenJava") {

      beforeEvaluate {
        artifactId = tasks.jar.get().archiveBaseName.get()
      }

      from(components["kotlin"])
      artifact(sourcesJar.get())

      // Provide artifacts information requited by Maven Central
      pom {

        name.set("Kotlin Beans Script")
        description.set("Support for .beans.kts files used for configuring Beans in Spring Boot")
        url.set("https://github.com/mrvanish97/kotlin-dsl-beans-extended")
        licenses {
          license {
            name.set("Eclipse Public License - v 2.0")
            url.set("https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("mrvanish97")
            name.set("Victor Mushtin")
            email.set("victormushtin@gmail.com")
          }
        }
        scm {
          connection.set("git@github.com:mrvanish97/kotlin-dsl-beans-extended.git")
          developerConnection.set("git@github.com:mrvanish97/kotlin-dsl-beans-extended.git")
          url.set("https://github.com/mrvanish97/kotlin-dsl-beans-extended")
        }
      }
    }
  }
}

// Signing artifacts. Signing.* extra properties values will be used

signing {
  sign(publishing.publications)
}
