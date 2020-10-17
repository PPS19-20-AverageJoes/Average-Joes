package AverageJoes.common

import AverageJoes.utils.IdGenerator._

trait Entity {
  def getId: String
}

abstract class BaseEntity(id: String) extends Entity {

  /** Secondary constructor **/
  def this(){
    this(generateUiid)
  }

  override def getId: String = id
}
