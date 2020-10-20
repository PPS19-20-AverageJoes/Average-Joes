package AverageJoes.common

import AverageJoes.utils.IdGenerator._

trait Entity {
  def getId: String
}

abstract class BaseEntity(id: String = generateUiid) extends Entity {

  override def getId: String = id
}
