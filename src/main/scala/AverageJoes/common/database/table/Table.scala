package AverageJoes.common.database.table

trait Customer extends Entity {
  val cf: String
  val name: String
  val surname: String
  val birthday: String
  val id: Int
}

//class Customer (name: String, age:Int) extends BaseEntity
class CustomerImpl(val cf:String, val name: String, val surname:String,
                   val birthday: String,  val id: Int) extends BasicEntity(id) with Customer {

}


trait Workout extends Entity{
  val customerID: Int
  val sets: Int
  val timer:Int
  val repetitions: Int
  val incline: Int
  val speed: Int
  val weight: Int
  val typeMachine: String
  val order: Int
  val id: Int

  def getCustomerID: Int = customerID
}

class WorkoutImpl(val customerID: Int, val sets: Int, val timer: Int,
                  val repetitions: Int, val incline: Int, val speed: Int,
                  val weight: Int, val typeMachine: String, val order: Int,
                  val id: Int) extends BasicEntity(id) with Workout {

  override def getCustomerID: Int = customerID
}