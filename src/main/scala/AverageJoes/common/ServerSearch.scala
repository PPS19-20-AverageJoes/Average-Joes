package AverageJoes.common

import AverageJoes.controller.GymController
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.ActorContext

trait ServerSearch {
  //import com.typesafe.config.Config
  //import com.typesafe.config.ConfigFactory
  //val config = ConfigFactory.parseFile(new Nothing("src/main/scala/AverageJoes/client.conf"))
  //val system: Nothing = ActorSystem.create("MySystem", config)
  //
  //def context : ActorContext
  def server: ActorRef[GymController.Msg] =
    {//ToDo: temporaneamente istanziato un gym controller
      ServerSearch.serverDummy
      //ActorSelection = context.actorSelection("akka://MySystem@127.0.0.1:25520/user/myActor")
      //public scala.concurrent.Future<ActorRef> resolveOne(Timeout timeout)
    }
}

object ServerSearch{
  private val actSystem = ActorSystem(Nothing,"Gym")
  val serverDummy: ActorRef[GymController.Msg] = GymController.startGymController(actSystem)
}