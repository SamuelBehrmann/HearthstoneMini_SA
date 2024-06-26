package persistence
package fileIO.jsonIOImpl

import model.fieldComponent.FieldInterface
import model.fieldComponent.fieldImpl.{Field}
import fileIO.FileIOInterface
import play.api.libs.json.*

import java.io.{File, PrintWriter}
import scala.io.Source
import scala.util.Try
import fileIO.FileIOInterface

class JsonIO extends FileIOInterface {
  override def load(): Try[FieldInterface] = Try {
    val source = Source.fromFile("field.json")
    val json = Json.parse(source.getLines().mkString)
    source.close()
    Field.fromJson(json)
  }

  override def save(field: FieldInterface): Unit = {
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(field.toJson))
    pw.close()
  }

  override def save(json: JsValue): Unit = {
    val pw = new PrintWriter(new File("field.json"))
    pw.write(Json.prettyPrint(json))
    pw.close()
  }
}
