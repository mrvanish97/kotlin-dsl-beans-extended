/*
 * Copyright (c) 2021 mrvanish97 [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
@file:SpringBootApplication(proxyBeanMethods = false)

package io.github.mrvanish97.kbnsext

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Profiles

rootConfiguration(
  name = ROOT_SCRIPT_CONFIG_NAME
)

bean {
  TestEntity(SCRIPT_ENTITY)
}

environment { false }.then {
  bean {
    TestEntity(NOT_EXISTING_SCRIPT_ENTITY)
  }
}.elseIf { true }.then {
  bean(ENV_SCRIPT_ENTITY) {
    TestEntity(ENV_SCRIPT_ENTITY)
  }
}.`else` {
  bean {
    TestEntity(NOT_EXISTING_SCRIPT_ENTITY)
  }
}

profile("non-existing") {
  bean {
    TestEntity(NOT_EXISTING_SCRIPT_ENTITY)
  }
}

if (environment.acceptsProfiles(Profiles.of("not-existing"))) {
  bean {
    TestEntity(context.id)
  }
}

bean {
  TestEntity(context.applicationName)
}

bean("${MISSING_ANNOTATED_SCRIPT_ENTITY}testEntity") {
  TestEntity(MISSING_ANNOTATED_SCRIPT_ENTITY)
}.annotate {
  with<ConditionalOnMissingClass> {
    it::value set String::class.javaName
  }
}

bean(LIST_OF_STRINGS) {
  mutableListOf<String>()
}

bean {
  TestEntity(ref<TestEntity>(ENV_SCRIPT_ENTITY).value + CONDITIONAL_PREFIX)
}.annotateWith<ConditionalOnClass> { it::value set String::class.java }

configuration(
  name = CONFIGURATION_BEAN_ANNOTATED,
  className = CONFIGURATION_BEAN_ANNOTATED_CLASS_NAME
) {
  bean(
    name = CONFIGURATION_BEAN_ANNOTATED_BEAN_NAME,
    methodName = CONFIGURATION_BEAN_ANNOTATED_METHOD_NAME
  ) {
    TestEntity(CONFIGURATION_BEAN_ANNOTATED_BEAN_NAME)
  }.annotateWith<Lazy>()
}.annotate {
  with<PropertySource> {
    it::value set ""
  }
}

init {
  ref<MutableList<String>>(LIST_OF_STRINGS).add(LIST_OF_STRINGS_ELEMENT)
}