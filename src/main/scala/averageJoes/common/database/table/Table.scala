package averageJoes.common.database.table

trait Customer extends Entity {
  val cf: String
  val name: String
  val surname: String
  val birthday: String
  val id: String
}

case class CustomerImpl(cf:String, name: String, surname:String, birthday: String, id: String) extends BasicEntity(id) with Customer


trait Workout extends Entity{
  val customerID: String
  val order: Int
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

case class WorkoutImpl(
                        customerID: String,
                        order: Int,
                        sets: Int,
                        timer: Int,
                        repetitions: Int,
                        incline: Int,
                        speed: Int,
                        weight: Int,
                        typeMachine: String,
                        secForSet: Int,
                        id: String) extends BasicEntity(id) with Workout {override def getCustomerID: String = customerID}