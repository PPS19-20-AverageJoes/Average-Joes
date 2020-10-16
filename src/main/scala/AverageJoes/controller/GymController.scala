package AverageJoes.controller

import AverageJoes.common.MsgActorMessage._
import AverageJoes.model.machine.MachineActor
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}

import scala.collection.mutable

object GymController {
  private var childUserActor = mutable.Map.empty[String, ActorRef[MsgUserRef]] //Child User
  private var childMachineActor = mutable.Map.empty[String, ActorRef[MachineActor.Msg]] //Child Machines

  sealed trait Msg
  object Msg{
    case class DeviceInGym(deviceID: String) extends Msg //Device enter in Gym
    case class UserLogin(userID: String) extends Msg //User logged
    case class PhysicalMachineWakeUp(machineID: String) extends Msg //Login to the controller
  }

  class GymController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context){

    override def receive: Receive = {
      case m: Msg.DeviceInGym => {
        val userID = m.deviceID //ToDo: recuperare utente relativo a device
        val user = context.actorOf(Props(new SmartGymUserImpl("","","","")), "actorUser") //ToDo: call constructor form user object
        childUserActor += ((userID,user))
        sender() ! MsgUserRef(user)
      }
      case m: Msg.UserLogin => {
        val user = childUserActor(m.userID) //ToDo: optional, gestire
        sender() ! MsgUserRef(user)
      }
      case m: Msg.PhysicalMachineWakeUp => {
        val machine = context.actorOf(Props(classOf[MachineActor], self), m.machineID) //ToDo: togliere serf una volta fatto il refactor del MachineActor
        childMachineActor += ((m.machineID, machine))
        sender() ! MsgMachineActorStarted(machine)
      }
    }
  }

  def startGymController(actorRefFactory: ActorRefFactory): ActorRef = actorRefFactory.actorOf(Props(classOf[GymController]), "GymController")

}