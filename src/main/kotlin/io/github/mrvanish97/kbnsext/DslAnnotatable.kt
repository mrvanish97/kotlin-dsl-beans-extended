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

typealias DslInit = AnnotationDsl.() -> Unit

abstract class DslAnnotatable<P> : Annotatable<DslInit>, AnnotationDataProvider<P> {

  protected abstract val kotlinTargets: KotlinTargets

  protected abstract val javaTargets: JavaTargets

  private val annotationsFromDsl = mutableListOf<Annotation>()

  override fun annotate(init: AnnotationDsl.() -> Unit) {
    val annotations = AnnotationDsl(kotlinTargets, javaTargets).apply(init).annotations
    annotationsFromDsl.apply {
      clear()
      addAll(annotations)
    }
  }

  fun buildAnnotations(externalParameter: P) = annotationsFromDsl.asSequence()
      .plus(annotationsFromParameters(externalParameter))
      .distinctBy { it.plainClass }
      .toList().also {
        checkDuplicatedAnnotations(it)
      }


}

inline fun <reified A : Annotation> Annotatable<DslInit>.annotateWith(
  noinline init: AnnotationBuilder<A>.(A) -> Unit = {}
) {
  annotate {
    with(init)
  }
}