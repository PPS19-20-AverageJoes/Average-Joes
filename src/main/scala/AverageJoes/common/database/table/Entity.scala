package AverageJoes.common.database.table

trait Entity{
  def getId: Int
}

abstract class BasicEntity(id: Int) extends Entity {

  def getId: Int = id
}

