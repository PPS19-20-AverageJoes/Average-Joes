package AverageJoes.model.hardware

import AverageJoes.common.{LogOnMessage, LoggableMsg, MachineTypes, ServerSearch}
import AverageJoes.controller.GymController
import AverageJoes.model.machine
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.mutable

object HardwareController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new HardwareController(context))

  //ToDo: servono le Map? O vanno bene le interrogazioni di akka typed?
  private var childD = mutable.Map.empty[String, ActorRef[Device.Msg]] //Child Devices
  private var childPM = mutable.Map.empty[(String, MachineTypes.MachineType), ActorRef[PhysicalMachine.Msg]] //Child Physical Machines

  sealed trait Msg extends LoggableMsg

  object Msg {

    //final case class PMActorStarted(machineID: String, phMachine: ActorRef[PhysicalMachine.Msg]) extends Msg
    final case class CreatePhysicalMachine(machineID: String, phMachineType: MachineTypes.MachineType, machineLabel: String) extends Msg

    final case class CreateDevice(deviceID: String, deviceType: Device.DeviceType.Type) extends Msg

    //final case class MachineActorStarted(machineID: String, phMachineType: PhysicalMachine.MachineType.Type, machineLabel: String, refMA: ActorRef[MachineActor.Msg]) extends Msg
  }

  class HardwareController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) with ServerSearch with LogOnMessage[Msg] {
    override val logName = "HW controller"
    override val loggingContext: ActorContext[Msg] = this.context

    override def onMessageLogged(msg: Msg): Behavior[Msg] = {
      msg match {
        case m: Msg.CreatePhysicalMachine =>
          if (childPM.keySet.exists(_._1 == m.machineID)) //ToDo: machine label deve essere univoco
          println("machineID already exists") //ToDo: inserire nel log
            else { //TODO: probabilmente la PM va creata contestualmente per gestire la presenza del codice univoco, rivedere la questione del machineactorref (la pm può entrare nel behaviour "ref waiting")
            val pm = context.spawn[PhysicalMachine.Msg](PhysicalMachine(m.machineID, m.phMachineType, m.machineLabel), m.machineID)
            childPM += (((m.machineID, m.phMachineType), pm))
            server ! GymController.Msg.PhysicalMachineWakeUp(m.machineID, m.phMachineType, pm)
          }
          Behaviors.same

        /*case m: Msg.MachineActorStarted =>
          val pm = context.spawn[PhysicalMachine.Msg](PhysicalMachine(m.phMachineType, m.refMA, m.machineID, m.machineLabel), m.machineID)
          childPM += (((m.machineID,m.phMachineType), pm))
          m.refMA ! MachineActor.Msg.PMActorStarted(pm)
          Behaviors.same*/

        case Msg.CreateDevice(deviceID, deviceType) =>
          val device = context.spawn[Device.Msg](Device(deviceType, deviceID), deviceID)
          childD += ((deviceID, device))
          Behaviors.same
      }
    }
  }

  def getChildDevice(name: String): Option[ActorRef[Device.Msg]] = {
    childD.get(name)
  }

  def getChildPmByName(name: String): Option[ActorRef[PhysicalMachine.Msg]] = {
    childPM.filterKeys(k => k._1 == name).last._2 match {
      case v: ActorRef[PhysicalMachine.Msg] => Some(v)
      case _ => None
    }
  }

}