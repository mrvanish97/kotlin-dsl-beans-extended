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

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBeanProvider
import kotlin.reflect.full.declaredFunctions

fun main(args: Array<String>) {
  val context = runApplication(*args)
  val test = BeansFileAnnotationTest(context, context.getBeanProvider<TestEntity>().toList())
  test::class.declaredFunctions
    .filter { it.annotations.asMerged().isPresent(Test::class.java) }
    .forEach { it.call(test) }
}