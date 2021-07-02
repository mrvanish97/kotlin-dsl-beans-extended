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

import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.ClassUtils

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

  override fun contextLoaded(context: ConfigurableApplicationContext) {
    val applicationContext = context as? GenericApplicationContext ?: return
    val classLoader = applicationContext.classLoader ?: return
    val scanner = ClassPathBeanScriptScanner(applicationContext.environment)
    for (basePackage in basePackages) {
      val beanDefinitions = scanner.findCandidateComponents(basePackage)
      val classNames = beanDefinitions.mapNotNull { it.beanClassName }
      for (className in classNames) {
        val clazz = try {
          classLoader.loadClass(className)
        } catch (e : ClassNotFoundException) {
          continue
        }
        val constructor = clazz.getConstructor(BeanDefinitionDsl::class.java) ?: continue
        val dsl = beans {  }
        // HACK : We need to do this 'pre-initialization' since there's no other ways to initialize context
        // otherwise, lateinit variable context in BeanDefinitionDsl won't be initialized, which leads to a corresponding Exception
        dsl.initialize(applicationContext)
        runCatching {
          constructor.newInstance(dsl)
        }
      }
    }
  }

}