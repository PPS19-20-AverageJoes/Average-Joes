package AverageJoes.common.database.table

import AverageJoes.utils.IdGenerator._

trait Entity {
  def getId: String = generateUiid
}

abstract class BaseEntity(id: String = generateUiid) extends Entity {

  override def getId: String = id
}
