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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.PropertySource

const val SCRIPT_ENTITY = "script-entity"
const val ENV_SCRIPT_ENTITY = "env-script-entity"
const val NOT_EXISTING_SCRIPT_ENTITY = "not-existing-script-entity"
const val CONDITIONAL_PREFIX = "-conditional"
const val ANNOTATED_CONDITIONAL_SCRIPT_ENTITY = ENV_SCRIPT_ENTITY + CONDITIONAL_PREFIX
const val MISSING_ANNOTATED_SCRIPT_ENTITY = "missing-annotated-env-script-entity"
const val LIST_OF_STRINGS = "list-of-strings-script-entity"
const val LIST_OF_STRINGS_ELEMENT = "element"
const val CONFIGURATION_BEAN_ANNOTATED = "configuration-bean-annotated"
const val CONFIGURATION_BEAN_ANNOTATED_CLASS_NAME = "ConfigBeanAnnotated"
const val CONFIGURATION_BEAN_ANNOTATED_METHOD_NAME = "getTestEntity"
const val CONFIGURATION_BEAN_ANNOTATED_BEAN_NAME = "configuration-inner-bean"
const val ROOT_SCRIPT_CONFIG_NAME = "root-script-config-name"

@SpringBootTest
class BeansFileAnnotationTest @Autowired constructor(
  private val context: ApplicationContext,
  private val testEntities: List<TestEntity>,
  @Qualifier(LIST_OF_STRINGS) private val listOfStrings: List<String>
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

  @Test
  fun rootScriptIsAnnotatedAndHasName() {
    assertNotNull(context.getBean(ROOT_SCRIPT_CONFIG_NAME))
  }

  @Test
  fun hasConfigurationInnerBean() {
    assertNotNull(context.getBean(CONFIGURATION_BEAN_ANNOTATED_BEAN_NAME))
  }

  @Test
  fun hasConfigurationBeanWithMethod() {
    assertNotNull(context.getBean(CONFIGURATION_BEAN_ANNOTATED)::class[CONFIGURATION_BEAN_ANNOTATED_METHOD_NAME])
  }

  @Test
  fun configurationBeanHasAnnotation() {
    assertNotNull(context.findAnnotationOnBean(CONFIGURATION_BEAN_ANNOTATED, PropertySource::class.java))
  }

  @Test
  fun hasNoBeanWithContextId() {
    assert(!isEntityPresented(context.id ?: ""))
  }

  @Test
  fun hasNoNonExistingBeans() {
    assert(!isEntityPresented(NOT_EXISTING_SCRIPT_ENTITY))
  }

  @Test
  fun hasElementInList() {
    assert(listOfStrings.contains(LIST_OF_STRINGS_ELEMENT))
  }

}