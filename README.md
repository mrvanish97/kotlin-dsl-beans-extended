# kotlin-dsl-beans-extended
A little library for using on your Spring Boot projects that add possibility to declare beans in Kotlin Script files.

It uses Spring's fresh new class `BeanDefinitionDsl` under the hood to provide easy and convinient way to organize your beans without messy `@get:Bean` annotations.
Obviously, this is designed to use on projects with Kotlin configured.

### Maven and gradle
Coming soon

### Usage example
After you added a library dependency, create a file under the source folder with extension `.beans.kts`
Let's imagine, the name of this file is `root.beans.kts` placed in `com.foo.example` package and it's empty.

First thing you have to do is to add `package com.foo.example`, otherwise it will be placed in the root package during the compilation and ignored by `@ComponentScan`

Then, write bean definitions as if it's a body of `init: BeanDefinitionDsl.() -> Unit` variable passed into the `BeanDefinitionDsl`'s `beans { ... }` function

``` Kotlin
package com.foo.example

bean {
  MyAmazingBean()
}

bean(name="anotherAmazingBean") {
  MyAmazingBean("Hello!")
}

profile(name="veryImportantProfileName") {
  bean {
    MyAmazingBean("I was created under the profile")
  }
}
```

By default, after your `.beans.kts` file has been created, you have to write `package` declaration everytime manually, which is quite frustrating.
That's why, I came up with IDEA Plugin, which automatically adds `package` into your `.beans.kts` file

### Intellij IDEA Plugin
<a href="https://github.com/mrvanish97/kotlin-dsl-beans-extended-plugin">Github repo</a>. As soon as plugin will appear on JetBranis Marketplace, I will update this section.
