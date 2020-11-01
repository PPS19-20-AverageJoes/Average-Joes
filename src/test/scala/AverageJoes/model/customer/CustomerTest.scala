package AverageJoes.model.customer

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import AverageJoes.common._
import AverageJoes.utils.DateUtils._

class CustomerTest extends AnyFlatSpec with Matchers {

  val c1 =  Customer("aabb00", "sokol", "guri", stringToDate("20/05/2020"))
  val c2 =  Customer("aabb01", "andrea", "rossi", stringToDate("10/04/2010"))
  val c3 =  Customer("aabb02", "elena", "bianchi", stringToDate("20/01/2010"))

  private var storage: Storage[Customer] = emptyStorage()

  "Customers storage" should "add customer" in {
    assert(storage.getCount == 0)
    storage add c1
    assert(storage.getCount == 1)
    storage add c2
    assert(storage.getCount == 2)

    storage = emptyStorage()
  }

  it should "find added customer" in {
    assert(storage.getCount == 0)
    storage add c1
    assert(c1 == storage.get(c1.getId).get)
    assert(c1.getId == storage.get(c1.getId).get.getId)
    assert(c2 !== storage.get(c1.getId).get)

    storage = emptyStorage()
  }


  it should "remove customer" in {
    assert(storage.getCount == 0)
    storage add c1
    storage add c2
    assert(storage.getCount == 2)
    storage remove  c2
    assert(storage.getCount == 1)

    storage = emptyStorage()
  }

  def emptyStorage() = new GymStorage[Customer]()

}