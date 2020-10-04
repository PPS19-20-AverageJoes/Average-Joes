package AverageJoes.controller

import AverageJoes.model.device.UserDevice
import AverageJoes.model.machine.PhysicalMachine
import akka.actor.{ActorRef, ActorSystem, Props}

case class HardwareController(private val actSystem: ActorSystem) {

  private var childPM = Map.empty[String, ActorRef] //Child Physical Machines
  private var childD = Map.empty[String, ActorRef] //Child Devices
/*
  def startPhysicalMachine(machineID: String, machineType: Class[_ <: PhysicalMachine]): ActorRef = {
    val machine = actSystem.actorOf(Props(machineType))
    childPM = childPM + (machineID -> machine)

    machine
  }

  def startDevice(deviceID: String, deviceType: Class[_ <: UserDevice]): ActorRef = {
    val device = actSystem.actorOf(Props(deviceType))
    childD = childD + (deviceID -> device)

    device
  }
  */
/*
  private def startActor(child: Map[String, ActorRef], id: String, classType:Class[_ <: AnyRef]): ActorRef ={
    val actor = actSystem.actorOf(Props(classType))
    child = child + (id -> actor)

    actor
  }*/
}

object HardwareController {
  //noinspection SpellCheckingInspection
  //TODO: implementare meglio e fare DRY con il GymController?
  /*
  private var _controller: Option[ActorRef] = None
  def controller(actSystem: ActorSystem): ActorRef = {
    if (_controller.isEmpty)
      _controller = Some(actSystem.actorOf(Props[HardwareController]))

    _controller.get
  }
*/


  //noinspection SpellCheckingInspection
//ogni macchina fisica deve avere un demone che pinga il server e si fa restituire l'actor ref del server e della macchina virtuale
}
