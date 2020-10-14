package AverageJoes

import AverageJoes.controller.GymController
import AverageJoes.model.user.UserObjectTester
import AverageJoes.view.View
import akka.actor.typed.ActorSystem

object GymApp extends App{
 // private val actSystem = ActorSystem("Gym")
 // private val controller = GymController.startGymController(actSystem)


  /*
  * import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
val config: Config = ConfigFactory.parseFile(new Nothing("src/main/scala/AverageJoes/server.conf"))
		  val system: Nothing = ActorSystem.create("MySystem", config)
  *
  * */



  ActorSystem(UserObjectTester(), "user-actor-system")
  private val view = new View
}
