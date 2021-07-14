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

interface Else<T> {

  fun `else`(init: () -> Unit)

}

interface ElseIf<T> : Else<T> {

  fun elseIf(predicate: T.() -> Boolean) : Then<T>

}

fun interface Then<T> {
  fun then(init: () -> Unit): ElseIf<T>
}

private object EmptyElseIf: ElseIf<Nothing> {

  override fun `else`(init: () -> Unit) {
    // should be empty
  }

  override fun elseIf(predicate: Nothing.() -> Boolean): Then<Nothing> {
    return EmptyThen
  }

}

private object EmptyThen: Then<Nothing> {
  override fun then(init: () -> Unit): ElseIf<Nothing> {
    return EmptyElseIf
  }
}

@PublishedApi
internal object SuccessfulThen : Then<Nothing> {
  override fun then(init: () -> Unit): ElseIf<Nothing> {
    init()
    return EmptyElseIf
  }
}

@PublishedApi
internal class FailedThen<T>(private val toTest: T) : Then<T> {
  override fun then(init: () -> Unit): ElseIf<T> {
    return object : ElseIf<T> {
      override fun `else`(init: () -> Unit) {
        init()
      }
      override fun elseIf(predicate: T.() -> Boolean): Then<T> {
        return if (toTest.predicate()) {
          @Suppress("UNCHECKED_CAST")
          SuccessfulThen as Then<T>
        } else {
          this@FailedThen
        }
      }
    }
  }
}

inline fun <T> T.`if`(predicate: T.() -> Boolean) : Then<T> {
  return if (predicate(this)) {
    @Suppress("UNCHECKED_CAST")
    SuccessfulThen as Then<T>
  } else {
    FailedThen(this)
  }
}