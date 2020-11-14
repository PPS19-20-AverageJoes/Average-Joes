package AverageJoes.controller

import AverageJoes.common.{LogOnMessage, LoggableMsg, MachineTypes}
import AverageJoes.model.customer.{CustomerActor, CustomerManager}
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.machine
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.MachineParameters
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable

object GymController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new GymController(context))

  private var childMachineActor = mutable.Map.empty[(String, MachineTypes.MachineType), ActorRef[MachineActor.Msg]] //Child Machines

  sealed trait Msg extends LoggableMsg
  object Msg{
    final case class DeviceInGym(customerID: String, replyTo: ActorRef[Device.Msg]) extends Msg //Device enter in Gym
    final case class UserLogin(customerID: String, replyTo:ActorRef[MachineActor.Msg]) extends Msg //User logged
    final case class PhysicalMachineWakeUp(machineID: String, phMachineType: MachineTypes.MachineType, replyTo: ActorRef[PhysicalMachine.Msg]) extends Msg //Login to the controller

    final case class CustomerRegistered(customerID: String, customer: ActorRef[CustomerActor.Msg]) extends Msg

    final case class MachinesToBookmark(phMachineType: MachineTypes.MachineType, replyTo: ActorRef[CustomerActor.Msg]) extends Msg

    final case class UserMachineWorkoutPlan(userID: String, exercise: Class[_ <: MachineParameters]) extends Msg
    final case class UserMachineWorkoutCompleted(user: ActorRef[MachineActor.Msg], exercise: Class[_ <: MachineParameters]) extends Msg
    final case class UserLogInStatus(status: Boolean) extends Msg
  }

  class GymController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) with LogOnMessage[Msg]{
    override val logName = "Gym controller"
    override val loggingContext: ActorContext[Msg] = this.context

    val customerManager: ActorRef[CustomerManager.Msg] = context.spawn(CustomerManager(), "CustomerManager")

    override def onMessageLogged(msg: Msg): Behavior[Msg] = {
      msg match {
        case m: Msg.DeviceInGym => customerManager ! CustomerManager.RequestCustomerCreation(m.customerID, context.self, m.replyTo); Behaviors.same

        case m: Msg.UserLogin => customerManager ! CustomerManager.RequestCustomerLogin(m.customerID, m.replyTo); Behaviors.same

        //case m: Msg.CustomerRegistered => childUserActor += ((m.customerID, m.customer)); Behaviors.same

        case m: Msg.PhysicalMachineWakeUp =>
          val machine = context.spawn[MachineActor.Msg](MachineActor(context.self, m.replyTo, m.phMachineType), "MA_"+m.machineID)
          childMachineActor += (((m.machineID, m.phMachineType), machine))
          m.replyTo ! PhysicalMachine.Msg.MachineActorStarted(m.machineID, machine)
          Behaviors.same

        case m: Msg.MachinesToBookmark => //ToDo: il messaggio va mandato al customer actor, che ciclerà sulla risposta
          //val children = getChildrenMachinesByType(m.phMachineType).toSet
          m.replyTo ! CustomerManager.MachineList(getChildrenMachinesByType(m.phMachineType).toSet)
          //if (children.nonEmpty) for (elem <- children) elem ! MachineActor.Msg.BookingRequest(m.replyTo)
          Behaviors.same
      }
    }
  }


  def getChildrenMachinesByType(pmType: MachineTypes.MachineType): Iterable[ActorRef[machine.MachineActor.Msg]] = {
    childMachineActor.filterKeys(k => k._2 == pmType).values
  }


}