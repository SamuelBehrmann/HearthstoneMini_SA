package core

import com.google.inject.AbstractModule
import com.google.inject.name.Names

import net.codingwell.scalaguice.ScalaModule
import controller.component.ControllerInterface
import controller.component.controllerImpl.Controller
import model.fieldComponent.FieldInterface
import model.fieldComponent.fieldImpl.Field
import persistence.fileIO.FileIOInterface
import persistence.fileIO.jsonIOImpl.JsonIO as json
class HearthstoneMiniModule extends AbstractModule {
  private val defaultSize = 5

  override def configure(): Unit = {
    bindConstant().annotatedWith(Names.named("DefaultSize")).to(defaultSize)
    bind(classOf[ControllerInterface]).to(classOf[Controller])
    bind(classOf[FieldInterface]).toInstance(new Field())
    bind(classOf[FileIOInterface]).to(classOf[json])
  }
}
