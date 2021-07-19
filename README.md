# kotlin-dsl-beans-extended
A small library for using on your Spring Boot projects that adds possibility to declare beans in Kotlin Script files.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.mrvanish97.kbnsext/kotlin-dsl-beans-extended/badge.svg#)](https://maven-badges.herokuapp.com/maven-central/io.github.mrvanish97.kbnsext/kotlin-dsl-beans-extended)

It's strongly inspired on Spring's fresh new `BeanDefinitionDsl` class.
Using this library can help you organize your beans without creating pointless `@Configuration` classes which only contain `@get:Bean` properties.
Obviously, this is designed to use on projects with Kotlin configured.

Since 0.2.0, it's possible to generate anonymous `@Configuration` classes right in the script.
Also, since 0.3.0, every bean created under the script's root is adding to the anonymous root configuration.

### Usage example
After you've added a library dependency, create a file under the source folder with extension `.beans.kts`
Let's imagine, the name of this file is `root.beans.kts` placed in `com.foo.example` package and it's empty.

First thing you have to do is to add `package com.foo.example`, otherwise it will be placed in the root package during the compilation and ignored by `@ComponentScan`

Then, write bean definitions as if it's a body of `init: BeanDefinitionDsl.() -> Unit` variable passed into the `BeanDefinitionDsl`'s `beans { ... }` function

```kotlin
package com.foo.example

bean {
  MyAmazingBean()
}

bean(name = "anotherAmazingBean", isLazyInit = true) {
  MyAmazingBean(
    text = "Hello!",
    context = context // for some very special reason
  )
}

profile("veryImportantProfileName") {
  bean {
    MyAmazingBean(
      text = "I was created under the profile",
      number = ref<BeanWithNumber>().value // use this for 'autowiring'
      extras = ref<ExtrasBean>("extrasBeanName") // and this one for wiring with 'qualifier'
    )
  }
}

// consider this as a body of afterPropertiesSet() method of the InitializingBean interface
init {
  ref<MyVeryInterestingRegistry>().add(RegistryElement())
}
```

Moreover, you are able to annotate your beans using ```annotate { }``` extension function on return values of`bean`, `configuration` and `rootConfiguration`

```kotlin
package com.foo.example

bean {
  MyAmazingBean("It looks like there's no chance I will be created :(")
}.annotateWith<ConditionalOnMissingClass> {   
  it::value set String::class.javaName 
}


bean {
  MyAmazingBean(
    text = "And I will! (Probably. Maybe this resource is not there...)",
    number = ref<BeanWithNumber>().value
  )
}.annotate {
  with<ConditionalOnResourse> {
    it::resources set "recource.that.surely.exists"
  }
  with<Primary>()
}
```

**Important note:** if you are adding a bean using `annotate`, you don't have to introduce `@Bean` or `@Component` annotations since they're added automatically. All necessary annotation arguments are being collected from values passed into `bean`, `component` or `rootComponent` functions.
Also, values for annotations `@Scope`, `@Lazy`, `@Primary`, `@Description`, `@Role` and `@Profile` can be defined by passing corresponding arguments into these functions.
However, you are still allowed to pick any of these styles but avoid declaring logically same bean modifiers using both approaches at the same time.
It will produce `IllegalArgumentException` with a message like `Duplicated annotations...`

```kotlin
package com.foo.example

bean(isLazyInit = true) {
  MyAmazingBean("will break everything during the context initialization")
}.annotate {
  with<Lazy>()
}

bean(description = "hello!") {
  MyAmazingBean("this is okay, no duplicating annotation data")
}.annotate {
  with<Lazy>()
  with<Primary>()
}

bean(isLazyInit = true) {
  MyAmazingBean("obviously, this is also OK")
}

```

It's also possible to create anonymous configuration classes.
When you are defining beans, they are actually created under the root configuration.
To have an access to its configuration, use `rootConfiguration` function.
**But note** that it can be used called no more than once. Otherwise, an exception will be thrown.
Let's take a look at the example:

```kotlin
package com.foo.example

rootConfiguration(
  name = "root",
  className = "Root"
).annotateWith<PropertySource> {
  it::value set "my.pros"
}
```
In case you annotate the script file using `@file:` annotations, they're going to be attached to the compiled class of your script.
Generally, it's possible to start up the Spring Boot application without any "plain" configuration classes and, instead of this, define corresponding annotations right in the script.
Be aware you need to run your Spring application using `runApplication(vararg args: String, init: SpringApplication.() -> Unit`) function and, at the same time, have to have `root.beans.kts` in main class' package
- Example 1:
  ```kotlin
  // com.foo.example.root.beans.kts

  @file:SpringBootApplication(proxyBeanMethods = false) 

  package com.foo.example
  ```
  ```kotlin
  // com.foo.example.main
  
  fun main(args: Array<String>) {
    runApplication(*args)
  }
  ```
If you want to use another one script, you can do the following:  
- Example 2
  ```kotlin
  // com.foo.example.main
  
  fun main(args: Array<String>) {
    runApplication(ScriptDefinition("com.foo.example.bar.script"), *args)
  }
  ```
The main thing it does is basically adding string reference to the compiled script class as a `source` for Spring Application
**Important note:** for the current version, it's not possible not to define `proxyBeanMethods = false` since it will lead to spring proxying errors.
I hope it will be fixed in the future. For solving this case, I opened a discussion on <a href="https://discuss.kotlinlang.org/t/how-to-compile-kotlin-script-into-open-not-final-java-class/22401">Kotlin forum</a>.

### Intellij IDEA Plugin
By default, after your `.beans.kts` file has been created, you have to write `package` declaration everytime manually, which is quite frustrating.
That's why, I came up with IDEA Plugin, which automatically adds `package` into your `.beans.kts` file

Here's the <a href="https://github.com/mrvanish97/kotlin-dsl-beans-extended-plugin">Github repo</a>. As soon as plugin will appear on JetBranis Marketplace, I will update this section.
