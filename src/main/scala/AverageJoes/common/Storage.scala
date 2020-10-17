package AverageJoes.common

trait Storage[E] {
  def add(e: E): List[E]
  def remove(e: E): List[E]
  def contains(e: E): Boolean
  def get(id: String): Option[E]
  def getAll: List[E]
  def getCount: Int

}

case class GymStorage[E<:Entity]() extends Storage[E] {

  protected var items: List[E] = List.empty

  def add(e: E): List[E] = if (contains(e)) throw DuplicateEntityException(e.getId); else { items = e :: items; items }

  def remove(e: E): List[E] = {items = items diff List(e); items}

  def contains(e: E): Boolean = items contains e

  def get(id: String): Option[E] = items find (_.getId == id)

  def getAll: List[E] = items

  override def getCount: Int = items size

}

case class DuplicateEntityException(existingE: String) extends RuntimeException


