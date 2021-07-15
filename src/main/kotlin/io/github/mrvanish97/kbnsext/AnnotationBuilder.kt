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
import kotlin.reflect.KClass
import kotlin.reflect.KProperty0

private val emptyAnnotationsCache = ConcurrentHashMap<Class<out Annotation>, Annotation>()

inline fun <reified A : Annotation> annotationBuilder() = AnnotationBuilder(A::class.java).build()
inline fun <reified A : Annotation> annotationBuilder(
  noinline initBuilder: AnnotationBuilder<A>.(A) -> Unit
) = AnnotationBuilder(A::class.java).applyBuilder(initBuilder)

class AnnotationBuilder<A : Annotation>(private val annotationClass: Class<out A>) {

  fun applyBuilder(initBuilder: AnnotationBuilder<A>.(A) -> Unit): A {
    initBuilder(this, this.emptyAnnotation)
    return build()
  }

  @Suppress("UNCHECKED_CAST")
  private val emptyAnnotation by lazy {
    emptyAnnotationsCache.getOrPut(annotationClass) {
      AnnotationUtils.synthesizeAnnotation(annotationClass)
    } as A
  }

  @PublishedApi
  internal val annotationAttributes = hashMapOf<String, Any>()

  private val annotationDelegate = lazy {
    runCatching {
      AnnotationUtils.synthesizeAnnotation(annotationAttributes, annotationClass, null)
    }
  }

  @PublishedApi
  internal fun build() = annotationDelegate.value.getOrThrow()

  infix fun <T : Any> KProperty0<T>.set(value: T?) {
    if (value == null) return
    checkAlreadyBuilt()
    annotationAttributes[name] = value
  }

  @JvmName("setOneElement")
  inline infix fun <reified T : Any> KProperty0<Array<T>>.set(value: T?) {
    if (value == null) return
    checkAlreadyBuilt()
    annotationAttributes[name] = arrayOf(value)
  }

  infix fun <T : Annotation> KProperty0<T>.set(builder: AnnotationBuilder<T>?) {
    if (builder == null) return
    checkAlreadyBuilt()
    annotationAttributes[name] = builder.annotationAttributes
  }


  @JvmName("setClass")
  infix fun <T : KClass<*>> KProperty0<T>.set(klass: KClass<*>?) {
    if (klass == null) return
    checkAlreadyBuilt()
    annotationAttributes[name] = klass.java
  }

  infix fun <T : Annotation> KProperty0<Array<T>>.set(builders: Array<AnnotationBuilder<T>>) {
    checkAlreadyBuilt()
    annotationAttributes[name] = builders.map { it.annotationAttributes }.toTypedArray()
  }

  @JvmName("setOneElement")
  infix fun <T : Annotation> KProperty0<Array<T>>.set(builder: AnnotationBuilder<T>) {
    checkAlreadyBuilt()
    annotationAttributes[name] = arrayOf(builder.annotationAttributes)
  }

  @PublishedApi
  internal fun checkAlreadyBuilt() {
    if (annotationDelegate.isInitialized()) {
      throw IllegalArgumentException("AnnotationBuilder is already built")
    }
  }

}