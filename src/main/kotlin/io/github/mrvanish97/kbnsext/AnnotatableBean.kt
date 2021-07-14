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

import org.springframework.context.annotation.*
import org.springframework.context.support.BeanDefinitionDsl
import java.lang.annotation.ElementType
import java.util.*

class AnnotatableBean<T : Any>(
  val name: String? = null,
  private val scope: BeanDefinitionDsl.Scope? = null,
  private val isLazyInit: Boolean? = null,
  private val isPrimary: Boolean? = null,
  private val isAutowireCandidate: Boolean? = null,
  private val description: String? = null,
  private val role: BeanDefinitionDsl.Role? = null,
  private val profile: Array<String>? = null,
  val methodName: String? = null,
  val destroyMethodName: String? = null,
  val beanClass: Class<out T>,
  val function: BeanSupplier.() -> T
) : DslAnnotatable<DestroyMethodNameProvider>(), Destroyable<Unit> {

  override val kotlinTargets = listOf(AnnotationTarget.FUNCTION)

  override val javaTargets = listOf(ElementType.METHOD)

  var onDestroyCallback: (() -> Unit)? = null
    private set

  override fun onDestroy(callback: () -> Unit) {
    onDestroyCallback = callback
  }

  override fun annotationsFromParameters(externalParameter: DestroyMethodNameProvider): List<Annotation> {
    if (onDestroyCallback == null && destroyMethodName != null) {
      throw IllegalArgumentException("destroyMethodName has been provided without 'onDestroy' callback")
    }
    val annotations = mutableListOf<Annotation>()
    val add = annotations.createAddFunction()
    annotationBuilder<Bean> {
      it::value set name
      it::autowireCandidate set isAutowireCandidate
      it::destroyMethod set (destroyMethodName
        ?: externalParameter.generateDestroyMethodName(this@AnnotatableBean))
    }.add()
    if (scope != null) {
      annotationBuilder<Scope> {
        it::scopeName set scope.name.lowercase(Locale.getDefault())
      }.add()
    }
    if (isLazyInit != null) {
      annotationBuilder<Lazy> {
        it::value set isLazyInit
      }.add()
    }
    if (isPrimary != null) {
      annotationBuilder<Primary>().add()
    }
    if (description != null) {
      annotationBuilder<Description> {
        it::value set description
      }.add()
    }
    if (role != null) {
      annotationBuilder<Role> {
        it::value set role.ordinal
      }.add()
    }
    if (profile != null) {
      annotationBuilder<Profile> {
        it::value set profile
      }.add()
    }
    return annotations
  }
}