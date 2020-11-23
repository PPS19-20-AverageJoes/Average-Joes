package averageJoes.common.database.table

trait Entity{
  def getId: String
}

abstract class BasicEntity(id: String) extends Entity {

  def getId: String = id
}

