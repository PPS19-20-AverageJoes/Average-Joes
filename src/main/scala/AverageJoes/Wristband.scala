package AverageJoes

import akka.actor.ActorRef

class Wristband(val userID: String, val userActor: ActorRef) extends UserDevice {
  /**/

  def display (s: String): Unit ={
    println(s)
  }

  def rfid : Unit ={
    val test = userID
  }

}
