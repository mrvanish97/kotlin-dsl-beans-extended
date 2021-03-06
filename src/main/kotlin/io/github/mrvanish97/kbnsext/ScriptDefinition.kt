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

data class ScriptDefinition(
  val packageName: String,
  val scriptName: String
) {

  constructor(fullScripName: String) : this(
    packageName = fullScripName.substringBeforeLast('.', ""),
    scriptName = fullScripName.substringAfterLast('.')
  )

  val className by lazy {
    "$packageName.${scriptName.capitalizeFirst()}${BeansScript.COMPILED_SCRIPT_SUFFIX}"
  }
}