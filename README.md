# kotlin-dsl-beans-extended
A small library for using on your Spring Boot projects that adds possibility to declare beans in Kotlin Script files.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.mrvanish97.kbnsext/kotlin-dsl-beans-extended/badge.svg#)](https://maven-badges.herokuapp.com/maven-central/io.github.mrvanish97.kbnsext/kotlin-dsl-beans-extended)

It uses Spring's fresh new `BeanDefinitionDsl` class under the hood to provide easy and convinient way to organize your beans without creating pointless `@Configuration` classes which only contain `@get:Bean` properties.
Obviously, this is designed to use on projects with Kotlin configured.

### Usage example
After you've added a library dependency, create a file under the source folder with extension `.beans.kts`
Let's imagine, the name of this file is `root.beans.kts` placed in `com.foo.example` package and it's empty.

First thing you have to do is to add `package com.foo.example`, otherwise it will be placed in the root package during the compilation and ignored by `@ComponentScan`

Then, write bean definitions as if it's a body of `init: BeanDefinitionDsl.() -> Unit` variable passed into the `BeanDefinitionDsl`'s `beans { ... }` function

``` Kotlin
package com.foo.example

bean {
  MyAmazingBean()
}

bean(name = "anotherAmazingBean", isLazyInit = true) {
  MyAmazingBean(
    text = "Hello!",
    context = applicationContext // for some very special reason
  )
}

profile(name = "veryImportantProfileName") {
  bean {
    MyAmazingBean(
      text = "I was created under the profile",
      number = ref<BeanWithNumber>().value // use this for 'autowiring'
      extras = ref<ExtrasBean>("extrasBeanName") // and this one for wiring with 'qualifier'
    )
  }
}
```

Moreover, you are able to annotate your beans using ```annotate { }```

``` Kotlin
package com.foo.example

annotate {
  with<ConditionalOnMissingClass> {
    it::value set String::class.javaName
  }
}.bean() {
  MyAmazingBean("It looks like there's no chance I will be created :(")
}

annotate {
  with<ConditionalOnResourse> {
    it::resources set "recource.that.surely.exists"
  }
  with<Primary>()
}.bean() {
  MyAmazingBean(
    text = "And I will! (Probably. Maybe this resource is not there...)",
    number = ref<BeanWithNumber>().value
  )
}
```

**Important note:** if you are adding a bean using `annotate`, you don't have to introduce `@Bean` annotation since it's added automatically.
Also, values for annotations `@Scope`, `@Lazy`, `@Primary`, `@Description` and `@Role` can be defined by passing corresponding arguments into `bean(...)` function.
However, you are still allowed to pick any of these styles but avoid declaring logically same bean modifiers using both approaches at the same time.
It will produce `IllegalStateException` with a message like `Duplicate annotation ${annotation_name}`

``` Kotlin
package com.foo.example

annotate {
  with<Lazy>()
}.bean(isLazyInit = true) {
  MyAmazingBean("will break everything during the context initialization")
}

annotate {
  with<Lazy>()
  with<Primary>()
}.bean(description = "hello!") {
  MyAmazingBean("this is okay, no duplicating annotation data")
}

bean(isLazyInit = true) {
  MyAmazingBean("obviously, this is also OK")
}

```


By default, after your `.beans.kts` file has been created, you have to write `package` declaration everytime manually, which is quite frustrating.
That's why, I came up with IDEA Plugin, which automatically adds `package` into your `.beans.kts` file

### Intellij IDEA Plugin
<a href="https://github.com/mrvanish97/kotlin-dsl-beans-extended-plugin">Github repo</a>. As soon as plugin will appear on JetBranis Marketplace, I will update this section.
