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
import org.springframework.beans.factory.config.BeanDefinitionCustomizer
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.context.annotation.*
import org.springframework.context.support.BeanDefinitionDsl
import org.springframework.context.support.GenericApplicationContext
import org.springframework.util.ClassUtils.CGLIB_CLASS_SEPARATOR
import java.lang.annotation.ElementType
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

const val BEANS_SCRIPT_EXTENSION = "beans.kts"

private val enhancerCounters = ConcurrentHashMap<String, AtomicInteger>()

@PublishedApi
internal val kotlinTargetClass = Target::class.java

@PublishedApi
internal val javaTargetClass = java.lang.annotation.Target::class.java

@KotlinScript(
  displayName = "Spring Beans Script",
  fileExtension = BEANS_SCRIPT_EXTENSION,
  compilationConfiguration = BeansScriptCompilationConfiguration::class,
)
abstract class BeansScript(
  @PublishedApi internal val basePackageName: String,
  @PublishedApi internal val context: GenericApplicationContext
) {

  @Suppress("unused")
  val BeanDefinitionDsl.BeanSupplierContext.applicationContext
    get() = context

  class AnnotationDsl internal constructor() {

    @PublishedApi
    internal val annotations = mutableListOf<Annotation>()

    inline fun <reified A : Annotation> with(initializer: AnnotationBuilder<A>.(A) -> Unit = {}) {
      val annotationClass = A::class.java
      val applicable = when {
        annotationClass.isAnnotationPresent(kotlinTargetClass) -> {
          val kotlinTarget = annotationClass.getAnnotation(kotlinTargetClass)
          kotlinTarget.allowedTargets.any {
            it == AnnotationTarget.FUNCTION
          }
        }
        annotationClass.isAnnotationPresent(javaTargetClass) -> {
          val javaTarget = annotationClass.getAnnotation(javaTargetClass)
          javaTarget.value.any {
            it == ElementType.METHOD
          }
        }
        else -> false
      }
      if (!applicable) {
        throw IllegalArgumentException("Annotation ${annotationClass.name} is not applicable neither for methods nor types")
      }
      addAnnotation(annotationBuilder(initializer))
    }

    @PublishedApi
    internal fun addAnnotation(annotation: Annotation): Boolean {
      return annotations.add(annotation)
    }

  }

  inner class AnnotatedBeanDsl internal constructor(@PublishedApi internal val annotations: List<Annotation>) {

    @PublishedApi
    internal fun Class<*>.makeGeneratedClassName(): String {
      val base = "${basePackageName}.${this.simpleName}_Configuration" +
        "${CGLIB_CLASS_SEPARATOR}ByteBuddyEnhanced${CGLIB_CLASS_SEPARATOR}"
      val number = enhancerCounters.getOrPut(base) { AtomicInteger(0) }.getAndIncrement()
      return "$base$number"
    }

    inline fun <reified T : Any> bean(
      name: String? = null,
      scope: BeanDefinitionDsl.Scope? = null,
      isLazyInit: Boolean? = null,
      isPrimary: Boolean? = null,
      isAutowireCandidate: Boolean? = null,
      initMethodName: String? = null,
      destroyMethodName: String? = null,
      description: String? = null,
      role: BeanDefinitionDsl.Role? = null,
      crossinline function: BeanDefinitionDsl.BeanSupplierContext.() -> T
    ) {
      val additionalAnnotations = mutableListOf<Annotation>()
      val beanName = name.toBeanName(T::class.java)
      val beanAnnotation = annotationBuilder<Bean> {
        it::name set beanName
        it::autowireCandidate set isAutowireCandidate
        it::initMethod set initMethodName
        it::autowireCandidate set isAutowireCandidate
        it::destroyMethod set destroyMethodName
      }
      additionalAnnotations.add(beanAnnotation)
      if (scope != null) {
        val scopeAnnotation = annotationBuilder<Scope> {
          it::scopeName set scope.name.lowercase(Locale.getDefault())
        }
        additionalAnnotations.add(scopeAnnotation)
      }
      if (isLazyInit != null) {
        val lazyAnnotation = annotationBuilder<Lazy> {
          it::value set isLazyInit
        }
        additionalAnnotations.add(lazyAnnotation)
      }
      if (isPrimary == true) {
        additionalAnnotations.add(annotationBuilder<Primary>())
      }
      if (description != null) {
        val descriptionAnnotation = annotationBuilder<Description> {
          it::value set description
        }
        additionalAnnotations.add(descriptionAnnotation)
      }
      if (role != null) {
        val roleAnnotation = annotationBuilder<Role> {
          it::value set role
        }
        additionalAnnotations.add(roleAnnotation)
      }
      val methodAnnotations = annotations.plus(additionalAnnotations).toTypedArray()
      val generatedConfigurationClass = ByteBuddy()
        .subclass(Any::class.java)
        .name(T::class.java.makeGeneratedClassName())
        .annotateType(annotationBuilder<Configuration>())
        .defineMethod(beanName.makeMethodName(), T::class.java, Modifier.PUBLIC)
        .intercept(InvocationHandlerAdapter.of { _, _, _ ->
          function(BeanDefinitionDsl.BeanSupplierContext(context))
        })
        .annotateMethod(*methodAnnotations)
        .make()
        .load(context.classLoader, ClassLoadingStrategy.Default.INJECTION)
        .loaded
      val configurationBeanName = null.toBeanName(generatedConfigurationClass)
      context.registerBean(configurationBeanName, generatedConfigurationClass, *emptyArray<BeanDefinitionCustomizer>())
    }

    @PublishedApi
    internal fun String?.toBeanName(clazz: Class<*>) = this ?: BeanDefinitionReaderUtils
      .uniqueBeanName(clazz.name, context)

  }

  fun annotate(init: AnnotationDsl.() -> Unit): AnnotatedBeanDsl {
    val dsl = AnnotationDsl()
    init(dsl)
    return AnnotatedBeanDsl(dsl.annotations)
  }

}

object BeansScriptCompilationConfiguration : ScriptCompilationConfiguration(body = {
  ide {
    acceptedLocations(ScriptAcceptedLocation.Everywhere)
  }
  implicitReceivers(KotlinType(BeanDefinitionDsl::class))
})