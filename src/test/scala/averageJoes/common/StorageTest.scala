package averageJoes.common

import averageJoes.common.database.{GymStorage, Storage}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StorageTest extends AnyFlatSpec with Matchers {

  val e1 = new UselessEntity("e1")
  val e2 = new UselessEntity("e2")

  private var storage: Storage[UselessEntity] = emptyStorage()

  "UselessEntity storage" should "add UselessEntities" in {
    assert(storage.getCount == 0)
    storage add e1
    assert(storage.getCount == 1)
    storage add e2
    assert(storage.getCount == 2)

    storage = emptyStorage()
  }

  it should "find added UselessEntities" in {
    assert(storage.getCount == 0)
    storage add e1
    assert(e1 == storage.get(e1.getId).get)
    assert(e1.getId == storage.get(e1.getId).get.getId)
    assert(e2 !== storage.get(e1.getId).get)

    storage = emptyStorage()
  }


  it should "remove UselessEntities" in {
    assert(storage.getCount == 0)
    storage add e1
    storage add e2
    assert(storage.getCount == 2)
    storage remove  e2
    assert(storage.getCount == 1)

    storage = emptyStorage()
  }


  def emptyStorage() = new GymStorage[UselessEntity]()

}
