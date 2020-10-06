package AverageJoes.controller

import AverageJoes.common.MsgActorMessage._
import AverageJoes.controller.GymController.GymController
import AverageJoes.model.device.Wristband
import AverageJoes.model.machine._
import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}

import scala.collection.mutable



object HardwareController {
  private var childD = mutable.Map.empty[String, ActorRef] //Child Devices
  private var childPM = mutable.Map.empty[String, ActorRef] //Child Physical Machines
  var gymController: ActorRef = null //ToDo: sostituire con ricerca controller in rete nell'init del controller

  private case class HardwareController() extends Actor{

    //private val userActor = context.actorOf(Props(SmartGymUserImpl("","","","")), "actorUser")

    private val wrist1 = Wristband.startWristband(context, "Wristband1")
    private val dLegPress1 = PhysicalMachine.startDemon(context, "LegPress1", classOf[LegPress])

    override def receive: Receive = {
      case m: MsgPMActorStarted => childPM.addOne(m.machineID, m.phMachine)
    }
  }

  def startHardwareController(actorRefFactory: ActorRefFactory): ActorRef ={
    actorRefFactory.actorOf(Props(classOf[HardwareController]), "HardwareController")
  }

}
