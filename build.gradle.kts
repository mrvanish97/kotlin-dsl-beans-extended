/*
 * Copyright (c) 2021 mrvanish97 [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.springframework.boot") version "2.5.2"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  kotlin("jvm") version "1.5.20"
  kotlin("plugin.spring") version "1.5.20"
  signing
  `maven-publish`
}

group = "io.github.mrvanish97.kbnsext"
version = "0.0.3"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter")

  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation("org.jetbrains.kotlin:kotlin-script-util")

  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

val javadocJar = "javadocJar"
task<Jar>(name = javadocJar) {
  archiveClassifier.set("javadoc")
  from(tasks["javadoc"])
}

val sourcesJar = "sourcesJar"
task<Jar>(name = sourcesJar) {
  archiveClassifier.set("sources")
  from(sourceSets["main"].allSource)
}

artifacts {
  archives(tasks[javadocJar])
  archives(tasks[sourcesJar])
}

tasks.withType<Sign> {
  sign(configurations["archives"])
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
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