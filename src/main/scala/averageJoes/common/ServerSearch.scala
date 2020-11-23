package averageJoes.common


import averageJoes.common.ControllerSearch.getController
import averageJoes.common.ServerSearch.{getServer, serverDummy}
import averageJoes.controller.GymController
import averageJoes.model.hardware.HardwareController
import averageJoes.model.hardware.HardwareController.Msg
import akka.actor.typed.{ActorRef, ActorSystem}

trait ServerSearch {
  def server: ActorRef[GymController.Msg] =
  {
    getServer
  }
}

object ServerSearch{
  var serverDummy:Option[ActorSystem[GymController.Msg]] =  Option.empty

  def getServer: ActorSystem[GymController.Msg] = {
    if(serverDummy.isEmpty){
      serverDummy =  Option.apply(ActorSystem(GymController(), "GymController"))
    }
    serverDummy.get
  }
 // private val serverDummy: ActorSystem[GymController.Msg] = ActorSystem(GymController(), "GymController")

  def closeSystem(): Unit = {
    serverDummy.get.terminate()
  }
}

trait ControllerSearch {
  def server: ActorRef[HardwareController.Msg] =
  {
    getController
  }
}

object ControllerSearch{
  var controllerDummy:Option[ActorSystem[HardwareController.Msg]] =  Option.empty


  def getController: ActorSystem[HardwareController.Msg] = {
    if(controllerDummy.isEmpty){
      controllerDummy =  Option.apply(ActorSystem(HardwareController(), "GymHardware"))
    }
    controllerDummy.get
  }
  // private val serverDummy: ActorSystem[GymController.Msg] = ActorSystem(GymController(), "GymController")

  def closeSystem(): Unit = {
    controllerDummy.get.terminate()
  }
}