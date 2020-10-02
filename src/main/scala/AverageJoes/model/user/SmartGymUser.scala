package AverageJoes.model.user

import AverageJoes.common.MsgUserLogged
import akka.actor.{Actor, ActorRef}

object SmartGymUser {
    def apply(name: String, surname: String, CF: String, userID: String): SmartGymUser = SmartGymUserImpl(name, surname, CF, userID)
}

case class SmartGymUserImpl(name: String, surname: String,  CF: String, userID: String) extends SmartGymUser with Actor {
    var logged = false

    override def isLogged: Boolean = logged
    override def logIn(to: ActorRef): Unit = { logged = true; self ! MsgUserLogged }
    override def logOut(): Unit =  logged = false

    override def receive: Receive = {
      case "AUTHENTICATE" => logIn(sender())
      case _ =>
    }
}