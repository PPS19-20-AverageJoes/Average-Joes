package AverageJoes.controller

import AverageJoes.common.MsgActorMessage._
import AverageJoes.common.ServerSearch
import AverageJoes.model.device._
import AverageJoes.model.machine._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable



object HardwareController {
  private var childD = mutable.Map.empty[String, ActorRef[Device.Msg]] //Child Devices
  private var childPM = mutable.Map.empty[String, ActorRef[PhysicalMachine.Msg]] //Child Physical Machines

  sealed trait Msg
  object Msg{
    case class PMActorStarted(machineID: String, phMachine: ActorRef[PhysicalMachine.Msg]) extends Msg
    case class CreatePhysicalMachine(refMA: ActorRef[MachineActor.Msg], machineID: String, phMachineType: PhysicalMachine.MachineType.Type) extends Msg
  }

  private case class HardwareController(override val context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) with ServerSearch {

    override def onMessage(msg: Msg): Behavior[Msg] = {
      msg match{
        case Msg.PMActorStarted(machineID, phMachine) => childPM += ((machineID, phMachine)); Behaviors.same
        case Msg.CreatePhysicalMachine(refMA, machineID, phMachineType) => {
          context.spawn[PMDaemon.Msg](PMDaemon(machineID, phMachineType),"name")
          Behaviors.same
        }

      }
    }

  }


  //Every PhysicalMachine need a daemon that tell the server of the starting up and retreive the actorref of the virtual Machine
  case class PMDaemon(machineID: String, phMachineType: PhysicalMachine.MachineType.Type) extends AbstractBehavior[PMDaemon.Msg](context: ActorContext[PMDaemon.Msg]) with ServerSearch{
    server ! GymController.Msg.PhysicalMachineWakeUp(machineID)

    override def onMessage(msg: Msg): Behavior[Msg] = {
      msg match{
        case PMDaemon.Msg.MachineActorStarted(refMA) => {
          val pm = context.spawn[PhysicalMachine.Msg](PhysicalMachine(phMachineType,refMA,machineID),"name")
          refMA ! PMActorStarted(machineID, pm)

          //ToDo: kill daemon
          Behaviors.same
        }
      }
    }

  }

  object PMDaemon{
    sealed trait Msg
    object Msg{
      case class MachineActorStarted(machine: ActorRef[MachineActor.Msg]) extends Msg
    }


  }

}
