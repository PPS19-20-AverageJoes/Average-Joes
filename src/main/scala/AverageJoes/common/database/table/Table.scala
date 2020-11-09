package AverageJoes.common.database.table

trait Customer extends BaseEntity {
  val cf: String
  val name: String
  val surname: String
  val birthday: String
  val id: Int
}

//class Customer (name: String, age:Int) extends BaseEntity
class CustomerImpl(val cf:String, val name: String, val surname:String,
                   val birthday: String,  val id: Int) extends Customer {

}
