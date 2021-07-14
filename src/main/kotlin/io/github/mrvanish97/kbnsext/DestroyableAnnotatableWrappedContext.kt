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

class DestroyableAnnotatableWrappedContext<Init>(
  private val destroyable: Destroyable<*>,
  private val annotatable: Annotatable<Init>
) : Destroyable<Annotatable<Init>>, Annotatable<Init> {

  override fun annotate(init: Init) {
    annotatable.annotate(init)
  }

  override fun onDestroy(callback: () -> Unit): Annotatable<Init> {
    destroyable.onDestroy(callback)
    return annotatable
  }

}