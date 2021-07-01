package by.mrvanish97.kbnsext

import org.springframework.context.support.BeanDefinitionDsl
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*

const val BEANS_SCRIPT_EXTENSION = "beans.kts"

@KotlinScript(
  displayName = "Spring Beans Script",
  fileExtension = BEANS_SCRIPT_EXTENSION,
  compilationConfiguration = BeansScriptCompilationConfiguration::class,
)
abstract class BeansScript

object BeansScriptCompilationConfiguration : ScriptCompilationConfiguration(body = {
  ide {
    acceptedLocations(ScriptAcceptedLocation.Everywhere)
  }
  implicitReceivers(KotlinType(BeanDefinitionDsl::class))
})