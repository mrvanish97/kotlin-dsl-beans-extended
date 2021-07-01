package by.mrvanish97.kbnsext

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext

const val SCRIPT_ENTITY = "script-entity"

@SpringBootTest
class BeansFileAnnotationTest @Autowired constructor(
  private val context: ApplicationContext,
  private val testEntities: List<TestEntity>,
) {

  private fun isEntityPresented(value: String): Boolean {
    return testEntities.any { it.value == value }
  }

  @Test
  fun contextInitialized() {
    assertNotNull(context)
  }

  @Test
  fun hasBeanDefinedByAnnotations() {
    assert(isEntityPresented(ANNOTATED_ENTITY))
  }

  @Test
  fun hasBeanDefinedByScript() {
    assert(isEntityPresented(SCRIPT_ENTITY))
  }

}