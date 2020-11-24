package averageJoes.common.database

import averageJoes.common.database.table.{Entity, Workout}

/**
 * Trait offering basic CRUD and query operations on a
 * data storage.
 */
trait Storage[E] {
  def add(e: E): List[E]
  def remove(e: E): List[E]
  def contains(e: E): Boolean
  def get(id: String): Option[E]
  def getAll: List[E]
  def getCount: Int
}

trait StorageWorkout[E] extends Storage[E]{
  def getWorkoutForCustomer(id: String): List[E]
}

class GymStorage[E<:Entity]() extends Storage[E] {

  protected var items: List[E] = List.empty

  def add(e: E): List[E] = if (contains(e)) items; else { items = e :: items; items }

  def remove(e: E): List[E] = {items = items diff List(e); items}

  def contains(e: E): Boolean = items contains e

  def get(id: String): Option[E] = items find (_.getId.equals(id))

  def getAll: List[E] = items

  def getCount: Int = items size
}

case class DuplicateEntityException(existingE: String) extends RuntimeException

class GymStorageWorkout[E <: Workout] extends GymStorage[E] with StorageWorkout[E]{
  def getWorkoutForCustomer(id: String): List[E] = { items.filter(_.customerID.equals(id))}
}

