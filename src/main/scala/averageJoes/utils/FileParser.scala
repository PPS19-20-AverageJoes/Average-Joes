package averageJoes.utils

import java.io.{BufferedWriter, FileWriter}

import averageJoes.common.database.Storage
import averageJoes.model.machine.FileWriterActor.WriteOnFile
import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, JArray, JValue, parse}

import scala.io.{BufferedSource, Source}

object FileParser {

  private def readFile(path: String): List[JValue] = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val source: BufferedSource = Source.fromFile(path)
    val lines: String = try source.mkString finally source.close()
    val json: JValue = parse(lines)
    val elements = json.children
    elements
  }

  def parsing[E](path: String, storage: Storage[E])(implicit m: Manifest[E]): Storage[E] = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val elements = readFile(path)
    for (acct <- elements) {
      val entity = acct.extract[E]
      storage.add(entity)
    }
    storage
  }

  def encoding(path: String, msg: WriteOnFile): Unit = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val elements = readFile(path)
    val newMsg: JValue = parse(write(msg))
    val array: JArray = JArray(elements.::(newMsg))
    val w = new BufferedWriter(new FileWriter(path))
    w.write(write(array))
    w.newLine()
    w.close()
  }

}
