package AverageJoes.controller

import AverageJoes.common.MsgActorMessage._
import AverageJoes.model.machine.MachineActor
import akka.actor.{Actor, ActorRef, ActorRefFactory, ActorSystem, Props}

import scala.collection.mutable

object GymController {
  private var childUserActor = mutable.Map.empty[String, ActorRef] //Child User
  private var childMachineActor = mutable.Map.empty[String, ActorRef] //Child Machines

  class GymController extends Actor{

    override def receive: Receive = {
      case m: MsgDeviceInGym => {
        val userID = m.deviceID //ToDo: recuperare utente relativo a device
        val user = context.actorOf(Props(new SmartGymUserImpl("","","","")), "actorUser") //ToDo: call constructor form user object
        childUserActor += ((userID,user))
        sender() ! MsgUserRef(user)
      }
      case m: MsgUserLogin => {
        val user = childUserActor(m.userID) //ToDo: optional, gestire
        sender() ! MsgUserRef(user)
      }
      case m: MsgPhysicalMachineWakeUp => {
        val machine = context.actorOf(Props(classOf[MachineActor], self), m.machineID) //ToDo: togliere serf una volta fatto il refactor del MachineActor
        childMachineActor += ((m.machineID, machine))
        sender() ! MsgMachineActorStarted(machine)
      }
    }
  }

  def startGymController(actorRefFactory: ActorRefFactory): ActorRef = actorRefFactory.actorOf(Props(classOf[GymController]), "GymController")

}