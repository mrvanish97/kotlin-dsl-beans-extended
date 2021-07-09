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

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.env.Environment
import org.springframework.core.io.ResourceLoader

private const val BEAN_SCRIPT_CLASS_PATTERN = "**/*_beans.class"

class ClassPathBeanScriptScanner(
  environment: Environment,
  resourceLoader: ResourceLoader
) : ClassPathScanningCandidateComponentProvider(false, environment) {

  init {
    this.resourceLoader = resourceLoader
    setResourcePattern(BEAN_SCRIPT_CLASS_PATTERN)
    addIncludeFilter { metadataReader, _ ->
      metadataReader.classMetadata.superClassName == BeansScript::class.java.name
    }
  }

}