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

import net.bytebuddy.ByteBuddy
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.InvocationHandlerAdapter
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.GenericApplicationContext
import java.lang.reflect.Modifier

abstract class AbstractConfigurationContainer(
  private val applicationContext: GenericApplicationContext
) {

  protected abstract val definition: AnnotatableConfiguration

  protected abstract val basePackage: String

  protected val beans = mutableListOf<AnnotatableBean<*>>()

  inline fun <reified T : Any> bean(
    name: String? = null,
    scope: BeanDefinitionDsl.Scope? = null,
    isLazyInit: Boolean? = null,
    isPrimary: Boolean? = null,
    isAutowireCandidate: Boolean? = null,
    destroyMethodName: String? = null,
    description: String? = null,
    role: BeanDefinitionDsl.Role? = null,
    methodName: String? = null,
    profile: String? = null,
    noinline function: BeanSupplier.() -> T
  ): DestroyableAnnotatableWrappedContext<DslInit> {
    return bean(
      name = name,
      scope = scope,
      isLazyInit = isLazyInit,
      isPrimary = isPrimary,
      isAutowireCandidate = isAutowireCandidate,
      destroyMethodName = destroyMethodName,
      description = description,
      role = role,
      methodName = methodName,
      profile = profile?.let { arrayOf(it) },
      function = function
    )
  }

  inline fun <reified T : Any> bean(
    name: String? = null,
    scope: BeanDefinitionDsl.Scope? = null,
    isLazyInit: Boolean? = null,
    isPrimary: Boolean? = null,
    isAutowireCandidate: Boolean? = null,
    destroyMethodName: String? = null,
    description: String? = null,
    role: BeanDefinitionDsl.Role? = null,
    methodName: String? = null,
    profile: Array<String>?,
    noinline function: BeanSupplier.() -> T
  ): DestroyableAnnotatableWrappedContext<DslInit> {
    val bean = AnnotatableBean(
      name = name,
      scope = scope,
      isLazyInit = isLazyInit,
      isPrimary = isPrimary,
      isAutowireCandidate = isAutowireCandidate,
      description = description,
      role = role,
      profile = profile,
      methodName = methodName,
      destroyMethodName = destroyMethodName,
      beanClass = T::class.java,
      function = function
    )
    proceedWithBean(bean)
    return DestroyableAnnotatableWrappedContext(bean, bean)
  }

  @PublishedApi
  internal fun proceedWithBean(bean: AnnotatableBean<*>) {
    beans.add(bean)
  }

  private fun destroyMethodNameProvider(destroyMethodName: String) = DestroyMethodNameProvider {
    if (it.onDestroyCallback != null) {
      destroyMethodName
    } else {
      ""
    }
  }

  protected abstract fun buildAnonymousConfigurationName(): String

  internal fun buildClassAndInject(): Class<*> {
    val className = definition.className ?: buildAnonymousConfigurationName()
    var classTemplate = ByteBuddy()
      .subclass(Any::class.java)
      .name("${basePackage}.${className}")
      .annotateType(*definition.buildAnnotations(Unit).toTypedArray())
    var methodCounter = 0
    beans.forEach {
      val beanMethodNameBase = if (it.name != null) {
        it.name.makeJavaName(true)
      } else {
        val methodSuffix = if (methodCounter == 0) {
          ""
        } else {
          methodCounter.toString()
        }
        methodCounter++
        "$className${it.beanClass.simpleName}$methodSuffix"
      }
      val beanMethodName = it.methodName ?: "get$beanMethodNameBase"
      val destroyMethodName = it.destroyMethodName ?: "onDestroy$beanMethodNameBase"
      val beanAnnotations = it.buildAnnotations(destroyMethodNameProvider(destroyMethodName))
      classTemplate = classTemplate
        .defineMethod(beanMethodName, it.beanClass, Modifier.PUBLIC)
        .intercept(InvocationHandlerAdapter.of { _, _, _ ->
          it.function(BeanSupplier(applicationContext))
        })
        .annotateMethod(*beanAnnotations.toTypedArray())
      val onDestroy = it.onDestroyCallback ?: return@forEach
      classTemplate = classTemplate
        .defineMethod(destroyMethodName, Unit::class.java, Modifier.PUBLIC)
        .intercept(InvocationHandlerAdapter.of { _, _, _ ->
          onDestroy()
        })
    }
    val generatedClass = classTemplate.make()
      .load(applicationContext.classLoader, ClassLoadingStrategy.Default.INJECTION)
      .loaded
    val configBeanName = definition.name ?: generateBeanMethodName(generatedClass)
    applicationContext.registerBean(configBeanName, generatedClass, *emptyArray())
    return generatedClass
  }

  private fun generateBeanMethodName(beanClass: Class<*>): String {
    return BeanDefinitionReaderUtils.uniqueBeanName(beanClass.name, applicationContext)
  }

}