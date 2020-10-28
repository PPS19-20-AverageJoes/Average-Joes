package AverageJoes.controller

import AverageJoes.common.{LogOnMessage, LoggableMsg, ServerSearch}
import AverageJoes.model.device._
import AverageJoes.model.machine
import AverageJoes.model.machine._
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

import scala.collection.mutable


object HardwareController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new HardwareController(context))

  //ToDo: servono le Map? O vanno bene le interrogazioni di akka typed?
  private var childD = mutable.Map.empty[String, ActorRef[Device.Msg]] //Child Devices
  private var childPM = mutable.Map.empty[(String, PhysicalMachine.MachineType.Type), ActorRef[PhysicalMachine.Msg]] //Child Physical Machines

  sealed trait Msg extends LoggableMsg
  object Msg{
    //final case class PMActorStarted(machineID: String, phMachine: ActorRef[PhysicalMachine.Msg]) extends Msg
    final case class CreatePhysicalMachine(machineID: String, phMachineType: PhysicalMachine.MachineType.Type) extends Msg
    final case class CreateDevice(deviceID: String, deviceType: Device.DeviceType.Type) extends Msg
    final case class MachineActorStarted(machineID: String, phMachineType: PhysicalMachine.MachineType.Type, machine: ActorRef[MachineActor.Msg]) extends Msg
  }

  class HardwareController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) with ServerSearch with LogOnMessage[Msg] {
    override val logName = "HW controller"
    override val loggingContext: ActorContext[Msg] = this.context

     override def onMessageLogged(msg: Msg): Behavior[Msg] = {
      msg match{
        case m: Msg.CreatePhysicalMachine =>
          server ! GymController.Msg.PhysicalMachineWakeUp(m.machineID, m.phMachineType, context.self)
          Behaviors.same

        case Msg.MachineActorStarted(machineID, phMachineType, refMA) =>
          val pm = context.spawn[PhysicalMachine.Msg](PhysicalMachine(phMachineType, refMA, machineID), machineID)
          childPM += (((machineID,phMachineType), pm))
          refMA ! MachineActor.Msg.PMActorStarted(pm)
          Behaviors.same

        case Msg.CreateDevice(deviceID, deviceType) =>
          val device = context.spawn[Device.Msg](Device(deviceType, deviceID), deviceID)
          childD += ((deviceID, device))
          Behaviors.same
      }
    }
  }

  def getChildDevice(name: String): Option[ActorRef[Device.Msg]] = {childD.get(name)}

  def getChildPmByName(name: String): Option[ActorRef[machine.PhysicalMachine.Msg]] = {
    childPM.filterKeys(k => k._1 == name).last._2 match {
      case v: ActorRef[machine.PhysicalMachine.Msg] => Some(v)
      case _ => None
    }
  }
//ToDo: spostare nel gym controller
  def getChildPmByType(pmType: PhysicalMachine.MachineType.Type): Option[ActorRef[PhysicalMachine.Msg]] = {
    childPM.filterKeys(k => k._2 == pmType).last._2 match {
      case v: ActorRef[machine.PhysicalMachine.Msg] => Some(v)
      case _ => None
    }
  }
}
