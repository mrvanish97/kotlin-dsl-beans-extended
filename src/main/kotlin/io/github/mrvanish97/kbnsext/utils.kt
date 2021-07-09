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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.reflect.KClass

fun <T, R> Iterable<T>.mapAsync(scope: CoroutineScope, mapper: suspend (T) -> R): List<Deferred<R>> {
  return map {
    scope.async {
      mapper(it)
    }
  }
}

fun String.makeMethodName(): String {
  val firstNonNumericalChar = indexOfFirst { !it.isDigit() }
  val withoutLeadingNumbers = substring(firstNonNumericalChar)
  return withoutLeadingNumbers.replace(Regex("[^\\w]"), "_")
}

val KClass<*>.javaName: String
  get() = java.name