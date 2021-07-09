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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass

bean {
  TestEntity(SCRIPT_ENTITY)
}

environment({ true }) {
  bean(ENV_SCRIPT_ENTITY) {
    TestEntity(ENV_SCRIPT_ENTITY)
  }
}

bean {
  TestEntity(applicationContext.applicationName)
}

annotate {
  with<ConditionalOnMissingClass> {
    it::value set String::class.javaName
  }
}.bean("${MISSING_ANNOTATED_SCRIPT_ENTITY}testEntity") {
  TestEntity(MISSING_ANNOTATED_SCRIPT_ENTITY)
}

annotate {
  with<ConditionalOnClass> { it::value set String::class.java }
}.bean {
  TestEntity(ref<TestEntity>(ENV_SCRIPT_ENTITY).value + CONDITIONAL_PREFIX)
}