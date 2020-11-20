package AverageJoes.controller

import AverageJoes.common.{LogManager, LoggableMsgTo, MachineTypes}
import AverageJoes.model.customer.{CustomerActor, CustomerManager}
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.{hardware, machine}
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.MachineParameters
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable

object GymController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new GymController(context))

  private var childMachineActor = mutable.Map.empty[(String, MachineTypes.MachineType), ActorRef[MachineActor.Msg]] //Child Machines

  private val logName = "Gym controller"
  sealed trait Msg extends LoggableMsgTo { override def To: String = logName }
  object Msg{
    //From Device
    final case class DeviceInGym(customerID: String, replyTo: ActorRef[Device.Msg]) extends Msg //Device enter in Gym
    //From MachineActor
    final case class UserLogin(customerID: String, machineLabel: PhysicalMachine.MachineLabel, pm: ActorRef[PhysicalMachine.Msg], replyTo:ActorRef[MachineActor.Msg]) extends Msg //User logged
    //From HardwareController
    final case class PhysicalMachineWakeUp(machineID: String, phMachineType: MachineTypes.MachineType, replyTo: ActorRef[PhysicalMachine.Msg]) extends Msg //Login to the controller
    //From CustomerActor & Co
    final case class CustomerRegistered(customerID: String, customer: ActorRef[CustomerActor.Msg]) extends Msg
    final case class MachinesToBookmark(phMachineType: MachineTypes.MachineType, replyTo: ActorRef[CustomerActor.Msg]) extends Msg

    //final case class UserMachineWorkoutPlan(userID: String, exercise: Class[_ <: MachineParameters]) extends Msg
    //final case class UserMachineWorkoutCompleted(user: ActorRef[MachineActor.Msg], exercise: Class[_ <: MachineParameters]) extends Msg
    //final case class UserLogInStatus(status: Boolean) extends Msg
  }

  private class GymController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) {

    val customerManager: ActorRef[CustomerManager.Msg] = context.spawn(CustomerManager(), "CustomerManager")

    override def onMessage(msg: Msg): Behavior[Msg] = {
      msg match {
        case m: Msg.DeviceInGym =>
          customerManager ! CustomerManager.RequestCustomerCreation(m.customerID, context.self, m.replyTo)
          m.replyTo ! Device.Msg.GoIdle()
          Behaviors.same

        case m: Msg.UserLogin =>  customerManager ! CustomerManager.RequestCustomerLogin(m.customerID, m.machineLabel, m.replyTo, m.pm); Behaviors.same

        case m: Msg.CustomerRegistered => Behaviors.same //ToDo: not used

        case m: Msg.PhysicalMachineWakeUp =>
          val machine = context.spawn[MachineActor.Msg](MachineActor(context.self, m.replyTo, m.phMachineType), "MA_"+m.machineID)
          childMachineActor += (((m.machineID, m.phMachineType), machine))
          m.replyTo ! PhysicalMachine.Msg.MachineActorStarted(m.machineID, machine)
          Behaviors.same

        case m: Msg.MachinesToBookmark =>
          //m.replyTo ! CustomerManager.MachineList(getChildrenMachinesByType(m.phMachineType).toSet)
          m.replyTo ! CustomerActor.MachineList(childMachineActor.filterKeys(k => k._2 == m.phMachineType).values.toSet)
          //Before was CustomerManager
          Behaviors.same

        case m: GymController.Msg => LogManager.log(logName+" Not Managed Message: "+m); Behaviors.same
      }
    }
  }

  //def getChildrenMachinesByType(pmType: MachineTypes.MachineType): Iterable[ActorRef[machine.MachineActor.Msg]] = { childMachineActor.filterKeys(k => k._2 == pmType).values }

}