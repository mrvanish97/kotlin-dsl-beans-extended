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

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

const val SCRIPT_ENTITY = "script-entity"
const val ENV_SCRIPT_ENTITY = "env-script-entity"
const val CONFUSING_PRIMARY_ANNOTATION_ENTITY = "confusing-primary-annotation-entity"
const val CONDITIONAL_PREFIX = "-conditional"
const val ANNOTATED_CONDITIONAL_SCRIPT_ENTITY = ENV_SCRIPT_ENTITY + CONDITIONAL_PREFIX
const val MISSING_ANNOTATED_SCRIPT_ENTITY = "missing-annotated-env-script-entity"

@SpringBootTest
class BeansFileAnnotationTest @Autowired constructor(
  private val context: ApplicationContext,
  private val testEntities: List<TestEntity>,
) {

  private fun isEntityPresented(value: String): Boolean {
    return testEntities.any { it.value == value }
  }

  @Test
  fun contextInitialized() {
    assertNotNull(context)
  }

  @Test
  fun hasBeanDefinedByScript() {
    assert(isEntityPresented(SCRIPT_ENTITY))
  }

  @Test
  fun hasBeanDefinedByScriptWithEnv() {
    assert(isEntityPresented(ENV_SCRIPT_ENTITY))
  }

  @Test
  fun hasNoBeanDefinedByScriptWithNonMatchingConditionInAnnotation() {
    assert(!isEntityPresented(MISSING_ANNOTATED_SCRIPT_ENTITY))
  }

  @Test
  fun hasBeanDefinedByScriptWithConditionalAnnotation() {
    assert(isEntityPresented(ANNOTATED_CONDITIONAL_SCRIPT_ENTITY))
  }

  @Test
  fun hasBeanWithApplicationName() {
    assert(isEntityPresented(context.applicationName))
  }

}