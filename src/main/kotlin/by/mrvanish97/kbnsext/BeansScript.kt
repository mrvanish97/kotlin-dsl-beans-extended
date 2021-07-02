/*******************************************************************************
 * Copyright (c) 2021 mrvanish97 [and others]
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package by.mrvanish97.kbnsext

import org.springframework.context.support.BeanDefinitionDsl
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

const val BEANS_SCRIPT_EXTENSION = "beans.kts"

@KotlinScript(
  displayName = "Spring Beans Script",
  fileExtension = BEANS_SCRIPT_EXTENSION,
  compilationConfiguration = BeansScriptCompilationConfiguration::class,
)
abstract class BeansScript

object BeansScriptCompilationConfiguration : ScriptCompilationConfiguration(body = {
  ide {
    acceptedLocations(ScriptAcceptedLocation.Everywhere)
  }
  implicitReceivers(KotlinType(BeanDefinitionDsl::class))
})