package AverageJoes.controller

import AverageJoes.common.ServerSearch
import AverageJoes.model.device._
import AverageJoes.model.machine._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import scala.collection.mutable


object HardwareController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new HardwareController(context))

  //ToDo: servono le Map? O vanno bene le interrogazioni di akka typed?
  private var childD = mutable.Map.empty[String, ActorRef[Device.Msg]] //Child Devices
  private var childPM = mutable.Map.empty[String, ActorRef[PhysicalMachine.Msg]] //Child Physical Machines

  sealed trait Msg
  object Msg{
    //final case class PMActorStarted(machineID: String, phMachine: ActorRef[PhysicalMachine.Msg]) extends Msg
    final case class CreatePhysicalMachine(machineID: String, phMachineType: PhysicalMachine.MachineType.Type) extends Msg
    final case class CreateDevice(deviceID: String, deviceType: Device.DeviceType.Type) extends Msg
    final case class MachineActorStarted(machineID: String, phMachineType: PhysicalMachine.MachineType.Type, machine: ActorRef[MachineActor.Msg]) extends Msg
  }

  class HardwareController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) with ServerSearch {

    override def onMessage(msg: Msg): Behavior[Msg] = {
      msg match{
        case m: Msg.CreatePhysicalMachine => {
          server ! GymController.Msg.PhysicalMachineWakeUp(m.machineID, m.phMachineType, context.self)
          //context.spawn[PMDaemon.Msg](PMDaemon(machineID, phMachineType), machineID)
          Behaviors.same
        }
        case Msg.MachineActorStarted(machineID, phMachineType, refMA) => {
          val pm = context.spawn[PhysicalMachine.Msg](PhysicalMachine(phMachineType, refMA, machineID), machineID)
          childPM += ((machineID, pm))
          refMA ! MachineActor.Msg.PMActorStarted(pm)
          Behaviors.same
        }
        case Msg.CreateDevice(deviceID, deviceType) => {
          val device = context.spawn[Device.Msg](Device(deviceType, deviceID), deviceID)
          childD += ((deviceID, device))
          Behaviors.same
        }
      }
    }
  }

  //Every PhysicalMachine need a daemon that tell the server of the starting up and retrieve the actorRef of the virtual Machine
  /*
  class PMDaemon(context: ActorContext[PMDaemon.Msg], machineID: String, phMachineType: PhysicalMachine.MachineType.Type)
    extends AbstractBehavior[PMDaemon.Msg](context) with ServerSearch{

    server ! GymController.Msg.PhysicalMachineWakeUp(machineID)

    override def onMessage(msg: PMDaemon.Msg): Behavior[PMDaemon.Msg] = {
      msg match{
        case PMDaemon.Msg.MachineActorStarted(refMA) => {
          val pm = context.spawn[PhysicalMachine.Msg](PhysicalMachine(phMachineType,refMA,machineID),"name")
          refMA ! MachineActor.Msg.PMActorStarted(pm)
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
   */

}
