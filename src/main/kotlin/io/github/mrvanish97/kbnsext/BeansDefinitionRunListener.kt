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

import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.ClassUtils
import java.util.concurrent.ConcurrentHashMap

private val loaded = ConcurrentHashMap.newKeySet<ConfigurableApplicationContext>()

class BeansDefinitionRunListener(
  application: SpringApplication,
  @Suppress("UNUSED_PARAMETER") args: Array<String>
) : SpringApplicationRunListener {

  private val basePackages = run {
    val appSources = application.allSources.filterIsInstance<Class<*>>()
    val sources = if (appSources.isNullOrEmpty()) {
      application.mainApplicationClass?.let { listOf(it) } ?: emptyList()
    } else {
      appSources
    }
    sources.map {
      val packages = AnnotationAttributes.fromMap(
        AnnotationMetadata.introspect(it)
          .getAnnotationAttributes(ComponentScan::class.java.name, true)
      )?.run {
        getStringArray(ComponentScan::value.name)
          .asSequence()
          .plus(getStringArray(ComponentScan::basePackages.name))
          .plus(getStringArray(ComponentScan::basePackageClasses.name).map { classFqName ->
            ClassUtils.getPackageName(classFqName)
          }).distinct().toList()
      }
      if (packages.isNullOrEmpty()) {
        listOf(ClassUtils.getPackageName(it))
      } else {
        packages
      }
    }.flatten().distinct()
  }

  private fun checkLoaded(context: ConfigurableApplicationContext): Boolean {
    var currentContext: ApplicationContext? = context
    while (currentContext != null) {
      if (loaded.contains(currentContext)) {
        return true
      }
      currentContext = currentContext.parent
    }
    return false
  }

  override fun contextLoaded(context: ConfigurableApplicationContext) {
    if (checkLoaded(context)) return
    loaded.add(context)
    if (context !is GenericApplicationContext) return
    val classLoader = context.classLoader ?: return
    val scanner = ClassPathBeanScriptScanner(context.environment, context)
    val classNameCache = ConcurrentHashMap.newKeySet<String>()
    runBlocking {
      basePackages.mapAsync(this) { basePackage ->
        val beanDefinitions = scanner.findCandidateComponents(basePackage)
        beanDefinitions.mapNotNull { it.beanClassName }
      }.mapAsync(this) {
        it.await().mapAsync(this) { className ->
          processClassName(classNameCache, classLoader, context, className)
        }.awaitAll()
      }.awaitAll()
    }
  }

  private fun processClassName(
    cache: MutableSet<String>,
    classLoader: ClassLoader,
    context: GenericApplicationContext,
    className: String
  ) {
    if (cache.contains(className)) return
    val clazz = try {
      classLoader.loadClass(className)
    } catch (e: ClassNotFoundException) {
      return
    }
    val constructor = clazz.getConstructor(GenericApplicationContext::class.java) ?: return
    if (!constructor.trySetAccessible()) return
    val script = constructor.newInstance(context) as BeansScript
    script.buildClasses()
    cache.add(className)
  }

}