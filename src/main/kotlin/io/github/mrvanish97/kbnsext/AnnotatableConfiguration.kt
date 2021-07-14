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

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.lang.annotation.ElementType

class AnnotatableConfiguration(
  val name: String? = null,
  private val proxyBeanMethods: Boolean? = null,
  val className: String? = null,
  private val profile: Array<String>? = null
) : DslAnnotatable<Unit>() {

  override val kotlinTargets = listOf(AnnotationTarget.CLASS)

  override val javaTargets = listOf(ElementType.TYPE)

  override fun annotationsFromParameters(externalParameter: Unit): List<Annotation> {
    val annotations = mutableListOf<Annotation>()
    val add = annotations.createAddFunction()
    annotationBuilder<Configuration> {
      it::value set name
      it::proxyBeanMethods set proxyBeanMethods
    }.add()
    if (profile != null) {
      annotationBuilder<Profile> {
        it::value set profile
      }
    }
    return annotations
  }
}