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

import io.github.mrvanish97.kbnsext.BeansScript.Companion.BEANS_SCRIPT_EXTENSION
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Profiles
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

@KotlinScript(
  displayName = "Spring Beans Script",
  fileExtension = BEANS_SCRIPT_EXTENSION,
  compilationConfiguration = BeansScriptCompilationConfiguration::class,
)
abstract class BeansScript(
  private val context: GenericApplicationContext
) : AbstractConfigurationContainer(context) {

  val environment
    get() = context.environment

  fun environment(predicate: ConfigurableEnvironment.() -> Boolean) = environment.`if`(predicate)

  fun profile(vararg profile: String, init: () -> Unit) = environment.`if` {
    acceptsProfiles(Profiles.of(*profile))
  }.then(init)

  companion object {
    private val rootConfigurationUpdater = AtomicReferenceFieldUpdater.newUpdater(
      BeansScript::class.java,
      AnnotatableConfiguration::class.java,
      BeansScript::rootUserConfiguration.name
    )
    private const val BEANS_SCRIPT_NAME = "beans"
    const val BEANS_SCRIPT_EXTENSION = "$BEANS_SCRIPT_NAME.kts"
    const val COMPILED_SCRIPT_SUFFIX = "_$BEANS_SCRIPT_NAME"
  }

  @Volatile
  private var rootUserConfiguration: AnnotatableConfiguration? = null

  private val thisScriptClassName = this::class.java.simpleName.substringBefore(COMPILED_SCRIPT_SUFFIX)

  private val defaultRootConfigurationClassName = "${thisScriptClassName}Configuration"

  override val definition: AnnotatableConfiguration
    get() = rootUserConfiguration ?: AnnotatableConfiguration()
  override val basePackage: String
    get() = this::class.java.packageName

  override fun buildAnonymousConfigurationName() = defaultRootConfigurationClassName

  fun rootConfiguration(
    name: String? = null,
    proxyBeanMethods: Boolean? = null,
    className: String? = null,
    profile: String? = null
  ): Annotatable<DslInit> {
    return rootConfiguration(name, proxyBeanMethods, className, profile?.let { arrayOf(it) })
  }

  fun rootConfiguration(
    name: String? = null,
    proxyBeanMethods: Boolean? = null,
    className: String? = null,
    profile: Array<String>?
  ): Annotatable<DslInit> {
    val config = AnnotatableConfiguration(name, proxyBeanMethods, className, profile)
    if (!rootConfigurationUpdater.compareAndSet(this, null, config)) {
      throw IllegalArgumentException("rootConfiguration should be called only once")
    }
    return AnnotatableWrappedContext(config)
  }

  private val innerAnonymousConfigurationCounter = AtomicInteger(0)

  private val innerContainers = mutableListOf<AbstractConfigurationContainer>()

  fun configuration(
    name: String? = null,
    proxyBeanMethods: Boolean? = null,
    className: String? = null,
    init: AbstractConfigurationContainer.() -> Unit
  ): Annotatable<DslInit> {
    val config = AnnotatableConfiguration(name, proxyBeanMethods, className)
    val container = object : AbstractConfigurationContainer(context) {
      override val definition = config
      override val basePackage = this@BeansScript.basePackage
      override fun buildAnonymousConfigurationName(): String {
        return "${thisScriptClassName}Configuration${innerAnonymousConfigurationCounter.getAndIncrement()}"
      }
    }
    container.apply(init)
    innerContainers.add(container)
    return AnnotatableWrappedContext(config)
  }

  internal fun buildClasses() {
    if (rootUserConfiguration != null || beans.isNotEmpty()) {
      listOf(this).plus(innerContainers)
    } else {
      innerContainers
    }.forEach { it.buildClassAndInject() }
  }

}

object BeansScriptCompilationConfiguration : ScriptCompilationConfiguration(body = {
  ide {
    acceptedLocations(ScriptAcceptedLocation.Everywhere)
  }
  compilerOptions("-P plugin:org.jetbrains.kotlin.allopen:preset=spring")
})