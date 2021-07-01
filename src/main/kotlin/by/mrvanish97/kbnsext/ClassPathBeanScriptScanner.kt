package by.mrvanish97.kbnsext

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.env.Environment

private const val BEAN_SCRIPT_CLASS_PATTERN = "**/*_beans.class"

class ClassPathBeanScriptScanner(
  environment: Environment
) : ClassPathScanningCandidateComponentProvider(false, environment) {

  init {
    setResourcePattern(BEAN_SCRIPT_CLASS_PATTERN)
    addIncludeFilter { metadataReader, _ ->
      metadataReader.classMetadata.superClassName == BeansScript::class.java.name
    }
  }

}