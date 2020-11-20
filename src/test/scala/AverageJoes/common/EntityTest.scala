package AverageJoes.common

import AverageJoes.common.database.table.{BasicEntity, Entity}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UselessEntity(name: String) extends BasicEntity("2") with Entity

case class EntityTest() extends AnyFlatSpec with Matchers {

    "UselessEntity " should "have random id" in {
      val entity =  new UselessEntity("e1")
      assert(entity.getId.equals("2"))
    }
}
