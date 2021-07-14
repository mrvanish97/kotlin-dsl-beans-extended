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
import org.springframework.core.annotation.MergedAnnotations
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

fun <T, R> Iterable<T>.mapAsync(scope: CoroutineScope, mapper: suspend (T) -> R): List<Deferred<R>> {
  return map {
    scope.async {
      mapper(it)
    }
  }
}

internal fun String.makeJavaName(capitalizeFirst: Boolean = false): String {
  val firstNonNumericalChar = indexOfFirst { !it.isDigit() }
  val withoutLeadingNumbers = substring(firstNonNumericalChar)
  val clean = withoutLeadingNumbers.replace(Regex("[^\\w]"), "_")
  return if (capitalizeFirst) {
    clean.capitalizeFirst()
  } else {
    clean
  }
}

val KClass<*>.javaName: String
  get() = java.name

fun String.capitalizeFirst() = replaceFirstChar {
  if (it.isLowerCase()) it.titlecase(
    Locale.getDefault()
  ) else it.toString()
}

operator fun KClass<*>.get(fieldName: String) = this.members.find { it.name == fieldName }

fun List<Annotation>.asMerged(): MergedAnnotations {
  return MergedAnnotations.from(*this.toTypedArray())
}

inline fun <reified A : Annotation> MergedAnnotations.has(): Boolean {
  return get(A::class.java).isPresent
}

fun <T> MutableCollection<T>.createAddFunction() : T.() -> Boolean {
  return fun T.(): Boolean {
    return add(this)
  }
}