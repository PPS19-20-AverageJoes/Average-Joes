package AverageJoes.controller

import AverageJoes.model.customer.CustomerManager
import AverageJoes.model.device.Device
import AverageJoes.model.machine.{MachineActor, PhysicalMachine}
import AverageJoes.model.workout.Exercise
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable

object GymController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new GymController(context))

  private var childUserActor = mutable.Map.empty[String, ActorRef[CustomerManager.Command]] //Child User
  private var childMachineActor = mutable.Map.empty[String, ActorRef[MachineActor.Msg]] //Child Machines

  sealed trait Msg
  object Msg{
    final case class DeviceInGym(deviceID: String, replyTo: ActorRef[Device.Msg]) extends Msg //Device enter in Gym
    final case class UserLogin(userID: String) extends Msg //User logged
    final case class PhysicalMachineWakeUp(machineID: String, phMachineType: PhysicalMachine.MachineType.Type, replyTo: ActorRef[HardwareController.Msg]) extends Msg //Login to the controller

    final case class UserMachineWorkoutPlan(userID: String, exercise: Class[_ <: Exercise]) extends Msg
    final case class UserMachineWorkoutCompleted(user: ActorRef[MachineActor.Msg], exercise: Class[_ <: Exercise]) extends Msg
    final case class UserLogInStatus(status: Boolean) extends Msg
  }

  class GymController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context){

    override def onMessage(msg: Msg): Behavior[Msg] = {
      msg match {
        case m: Msg.DeviceInGym => {
          val userID = m.deviceID //ToDo: recuperare utente relativo a device
          //val user = context.actorOf(Props(new SmartGymUserImpl("", "", "", "")), "actorUser") //ToDo: call constructor form user object
          //childUserActor += ((userID, user))
          //m.replyTo ! Device.Msg.UserRef(user)
          Behaviors.same
        }
        /*case m: Msg.UserLogin => {
          val user = childUserActor(m.userID) //ToDo: optional, gestire
          sender() ! MsgUserRef(user)
        }*/
        case m: Msg.PhysicalMachineWakeUp => {
          val machine = context.spawn[MachineActor.Msg](MachineActor(context.self, m.phMachineType), m.machineID)
          childMachineActor += ((m.machineID, machine))
          m.replyTo ! HardwareController.Msg.MachineActorStarted(m.machineID, m.phMachineType, machine)
          Behaviors.same
        }
      }
    }
  }


}