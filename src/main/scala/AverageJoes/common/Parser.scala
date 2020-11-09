package AverageJoes.common

import AverageJoes.common.database.Storage
import net.liftweb.json.{DefaultFormats, _}

import scala.io.{BufferedSource, Source}

object Parser extends App {

  def parsing[E](path: String, storage: Storage[E])(implicit m: Manifest[E]): Storage[E] = {
    implicit val formats: DefaultFormats.type = DefaultFormats

    val source: BufferedSource = Source.fromFile(path)
    val lines: String = try source.mkString finally source.close()
    val json: JValue = parse(lines)
    val elements = json.children
    for (acct <- elements) {
      val entity = acct.extract[E]
      storage.add(entity)
    }
    storage
  }

}