package AverageJoes.common.storage

trait Entity {
  def getId: String
}

abstract class BaseEntity(id: String) extends Entity{

  /** Secondary constructor **/
  def this(){
    this(java.util.UUID.randomUUID.toString)
  }

  override def getId: String = id
}
