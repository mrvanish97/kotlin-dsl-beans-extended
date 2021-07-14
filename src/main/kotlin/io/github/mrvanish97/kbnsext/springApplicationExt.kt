/*
 * Copyright (c) 2021 mrvanish97 [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package io.github.mrvanish97.kbnsext

import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext

private val empty: SpringApplication.() -> Unit = {}

fun runApplication(vararg args: String, init: SpringApplication.() -> Unit = empty): ConfigurableApplicationContext {
  return SpringApplication().apply(init).apply {
    sources.add(ScriptDefinition(mainApplicationClass.packageName, "root").className)
  }.run(*args)
}

fun runApplication(
  scriptDefinition: ScriptDefinition,
  vararg args: String,
  init: SpringApplication.() -> Unit = empty
): ConfigurableApplicationContext {
  return SpringApplication().apply(init).apply {
    sources.add(scriptDefinition.className)
  }.run(*args)
}
