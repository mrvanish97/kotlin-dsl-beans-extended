package by.mrvanish97.kbnsext

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val ANNOTATED_ENTITY = "annotated-entity"

@Configuration
class AnnotatedConfig {

  @get:Bean
  val testEntity
    get() = TestEntity(ANNOTATED_ENTITY)

}