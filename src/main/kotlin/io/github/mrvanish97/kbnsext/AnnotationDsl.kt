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


private val kotlinTargetClass = Target::class.java

private val javaTargetClass = java.lang.annotation.Target::class.java

private val annotationJavaClass = Annotation::class.java

val <A : Annotation> A.plainClass: Class<out A>
  get() {
    val clazz = this::class.java
    val firstInterface = clazz.interfaces.first()
    return if (annotationJavaClass.isAssignableFrom(firstInterface) && annotationJavaClass != firstInterface) {
      @Suppress("UNCHECKED_CAST")
      firstInterface as Class<out A>
    } else {
      clazz
    }
  }

fun checkDuplicatedAnnotations(annotations: List<Annotation>) {
  val annotationsClasses = annotations.map { it::class.java }
  val duplications = annotationsClasses
    .distinct()
    .map { annotationClass ->
      Pair(annotationClass, annotationsClasses.count { annotationClass == it })
    }.filter { it.second > 1 }
  if (duplications.isNotEmpty()) {
    val duplicatedMessage = duplications.joinToString(separator = "\n") { it.first.name }
    throw IllegalArgumentException(
      "You are not allowed to declare multiple annotations of the same type." +
        "Consider using only distinct ones or replace it with @Repeatable ones. " +
        "Duplicated annotation(s):\n$duplicatedMessage"
    )
  }
}

class AnnotationDsl(
  private val kotlinTargets: KotlinTargets,
  private val javaTargets: JavaTargets
) {

  private fun checkDuplicatedAnnotations() {
    checkDuplicatedAnnotations(annotationsInternal)
  }

  private fun checkAnnotationApplicable(
    annotationClass: Class<out Annotation>
  ) {
      val applicable = when {
        annotationClass.isAnnotationPresent(kotlinTargetClass) -> {
          val kotlinTarget = annotationClass.getAnnotation(kotlinTargetClass)
          kotlinTarget.allowedTargets.any {
            kotlinTargets.contains(it)
          }
        }
        annotationClass.isAnnotationPresent(javaTargetClass) -> {
          val javaTarget = annotationClass.getAnnotation(javaTargetClass)
          javaTarget.value.any {
            javaTargets.contains(it)
          }
        }
        else -> false
      }
    if (!applicable) {
      throw IllegalArgumentException("Annotation ${annotationClass.name} is not applicable for targets $kotlinTargets or $javaTargets")
    }
  }

  private val annotationsInternal = mutableListOf<Annotation>()
  internal val annotations
    get() = annotationsInternal.toList()

  inline fun <reified A : Annotation> with(noinline initializer: AnnotationBuilder<A>.(A) -> Unit = {}) {
    with(A::class.java, initializer)
  }

  @PublishedApi
  internal fun <A : Annotation> with(
    annotationClass: Class<out A>,
    initializer: AnnotationBuilder<A>.(A) -> Unit = {}
  ) {
    val annotation = AnnotationBuilder(annotationClass).applyBuilder(initializer)
    checkAnnotationApplicable(annotation.plainClass)
    addAnnotation(annotation)
    checkDuplicatedAnnotations()
  }

  private fun addAnnotation(annotation: Annotation): Boolean {
    return annotationsInternal.add(annotation)
  }

}