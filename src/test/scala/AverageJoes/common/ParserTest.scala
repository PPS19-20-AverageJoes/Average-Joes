package AverageJoes.common

import AverageJoes.common.database.{Database,  GymStorage, Storage}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ParserTest extends AnyFlatSpec with Matchers {
  val path: String = "src/main/resources/customer.json"

  "Storage" should "not be empty" in {
    /*var storage = CustomerDatabase.customerStorage
    storage = Parser.parsing(path, storage)*/

    assert(Database.customerStorage.getCount > 0)
  }

  "Customer" should "have different id" in {
    /*var storage = CustomerDatabase.customerStorage
    storage = Parser.parsing(path, storage)*/
    //val cust = new Customer("Erika", 3)
    for(customer <- Database.customerStorage.getAll) println(customer.name)
    //assert(!customer.getId.equals(cust.getId))
  }
}

