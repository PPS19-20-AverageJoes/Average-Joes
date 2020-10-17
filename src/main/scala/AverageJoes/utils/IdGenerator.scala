package AverageJoes.utils

import scala.annotation.tailrec
import scala.util.Random

object IdGenerator extends UiidGen {
  override val key: String = "AverageJoes"

}

object RANStream {
  val randomAlphaNumIterator: Iterator[Char] = Random.alphanumeric.iterator

  @tailrec
  def getRandomString(length: Int, acc: String = ""): String = {
    require(length >= 0, message = "length needs to be non-negative")
    if (length == 0) acc
    else getRandomString(length - 1, randomAlphaNumIterator.next().toString)
  }
}

trait UiidGen {

  val key: String

  def generateUiid: String = {
    val longTime = System.currentTimeMillis()
    val hexTime = longTime.toHexString

    (key + "_" + hexTime + "_" + RANStream.getRandomString(32))
      .substring(0, 20)
  }

}

/**
 * https://gist.github.com/sarveshseri/f188a1a52ff966c63ea4
 */
object Main extends App {
  print(IdGenerator.generateUiid)
}
