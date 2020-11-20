package AverageJoes.common.database.table

trait Customer extends Entity {
  val cf: String
  val name: String
  val surname: String
  val birthday: String
  val id: String
}

//class Customer (name: String, age:Int) extends BaseEntity
case class CustomerImpl(val cf:String, val name: String, val surname:String,
                   val birthday: String,  val id: String) extends BasicEntity(id) with Customer {

}


trait Workout extends Entity{
  val customerID: String
  val sets: Int
  val timer:Int
  val repetitions: Int
  val incline: Int
  val speed: Int
  val weight: Int
  val typeMachine: String
  val secForSet: Int
  val id: String

  def getCustomerID: String = customerID
}

case class WorkoutImpl(val customerID: String, val sets: Int, val timer: Int,
                  val repetitions: Int, val incline: Int, val speed: Int,
                  val weight: Int, val typeMachine: String, val secForSet: Int,
                  val id: String) extends BasicEntity(id) with Workout {

  override def getCustomerID: String = customerID
}