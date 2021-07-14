# Changelog

### 0.3.1
- Fixed beans creation in root configuration while not specifying anything in `rootConfiguration`

### 0.3.0

- Almost got rid of `BeanDefinitionDsl` in favor of fully-runtime configuration definitions.
  Conditions on environment or profiles now can be achieved using `environment` and `profile` functions.
  Example:
  ```kotlin
  environment {
    get("prop-to-check") != null
  }.then {
    configuration { /*...*/ }
  }.`else` {
    configuration { /*...*/ }
  }
  ```
- Since I'm the only one for this moment who is using this library, I've changed the way things are annotated.
  Now, instead of `annotate` function on the top level, we've got it now being an extension function for `bean`, `configuration` and `rootConfiguration` return values
  ```kotlin
    bean {
      MyAmazingBean()
    }.annotate {
      with<SuperAnnotation()> { it::value set "wow" }
    }
  ```
- `runApplication` is added to support .beans.script as a property source.
  Just place `root.beans.kts` file with `@file:SpringBootApplication(proxyBeanMethods=false)` in the main class' package at voilà.
  **Note:** Not adding `proxyBeanMethods=false` into the `@Configuration` will lead to failing the startup, since the generated script class is final.
  I'm trying to find a way to define it as `open`, will wait for the Kotlin community support on this question

### 0.2.0

- Definition of configuration classes added using `configutation` annotating with `annotate` function.
Also, all the beans defined in script now generated under the root configuration, which can be annotated using annotations on script file e.g. `@file:Annotation`

### 0.1.0

- `annotate` function has bean added to add various annotations on your bean as if they were defined in `@Configuration` class using annotations on `@Bean`-annotated method
- Changelog added (sic!)

### 0.0.4

- `profile` and `environment` are fixed. They were simply ignored by scanning

### 0.0.3

- Essential `BeanDefinitionDsl` methods functionality is supported in `.beans.kts` script
- Deployed on Maven Central Repository