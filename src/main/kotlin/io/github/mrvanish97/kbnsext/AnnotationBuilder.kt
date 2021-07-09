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

import org.springframework.core.annotation.AnnotationUtils
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty0

private val emptyAnnotationsCache = ConcurrentHashMap<Class<out Annotation>, Annotation>()

inline fun <reified A : Annotation> annotationBuilder() = AnnotationBuilder(A::class.java).build()
inline fun <reified A : Annotation> annotationBuilder(
  initBuilder: AnnotationBuilder<A>.(A) -> Unit
) = AnnotationBuilder(A::class.java).apply {
  initBuilder(this, this.emptyAnnotation)
}.build()

class AnnotationBuilder<A : Annotation>(private val annotationClass: Class<out A>) {

  @PublishedApi
  @Suppress("UNCHECKED_CAST")
  internal val emptyAnnotation by lazy {
    emptyAnnotationsCache.getOrPut(annotationClass) {
      AnnotationUtils.synthesizeAnnotation(annotationClass)
    } as A
  }

  @PublishedApi
  internal val annotationAttributes = hashMapOf<String, Any>()

  private val annotationDelegate = lazy {
    AnnotationUtils.synthesizeAnnotation(annotationAttributes, annotationClass, null)
  }

  @PublishedApi
  internal fun build() = annotationDelegate.value

  infix fun <T : Any> KProperty0<T>.set(value: T?) {
    if (value == null) return
    checkAlreadyBuild()
    annotationAttributes[name] = value
  }

  @JvmName("setOneElement")
  inline infix fun <reified T : Any> KProperty0<Array<T>>.set(value: T?) {
    if (value == null) return
    checkAlreadyBuild()
    annotationAttributes[name] = arrayOf(value)
  }

  infix fun <T : Annotation> KProperty0<T>.set(builder: AnnotationBuilder<T>?) {
    if (builder == null) return
    checkAlreadyBuild()
    annotationAttributes[name] = builder.annotationAttributes
  }

  infix fun <T : Annotation> KProperty0<Array<T>>.set(builders: Array<AnnotationBuilder<T>>) {
    checkAlreadyBuild()
    annotationAttributes[name] = builders.map { it.annotationAttributes }.toTypedArray()
  }

  @JvmName("setOneElement")
  infix fun <T : Annotation> KProperty0<Array<T>>.set(builder: AnnotationBuilder<T>) {
    checkAlreadyBuild()
    annotationAttributes[name] = arrayOf(builder.annotationAttributes)
  }

  @PublishedApi
  internal fun checkAlreadyBuild() {
    if (annotationDelegate.isInitialized()) {
      throw IllegalArgumentException("AnnotationBuilder is already built")
    }
  }

}