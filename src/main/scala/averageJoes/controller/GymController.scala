package averageJoes.controller

import averageJoes.common.LoggableMsgTo
import averageJoes.model.customer.{CustomerActor, CustomerManager}
import averageJoes.model.hardware.PhysicalMachine.MachineLabel
import averageJoes.model.hardware.{Device, PhysicalMachine}
import averageJoes.model.machine.MachineActor
import averageJoes.model.workout.MachineTypes
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

object GymController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new GymController(context))

  private var childCustomerActor = Map.empty[String, ActorRef[CustomerActor.Msg]] //Child Customer (for test purpose only)
  private var childMachineActor = Map.empty[(String, MachineTypes.MachineType), ActorRef[MachineActor.Msg]] //Child Machines

  private val logName = "Gym controller"
  sealed trait Msg extends LoggableMsgTo { override def To: String = logName }
  object Msg{
    //From Device
    final case class DeviceInGym(customerID: String, replyTo: ActorRef[Device.Msg]) extends Msg //Device enter in Gym
    //From MachineActor
    final case class UserLogin(customerID: String, machineLabel: PhysicalMachine.MachineLabel, phMachineType: MachineTypes.MachineType, pm: ActorRef[PhysicalMachine.Msg], replyTo:ActorRef[MachineActor.Msg]) extends Msg //User logged
    //From HardwareController
    final case class PhysicalMachineWakeUp(machineID: String, machineLabel: MachineLabel, phMachineType: MachineTypes.MachineType, replyTo: ActorRef[PhysicalMachine.Msg]) extends Msg //Login to the controller
    //From CustomerActor & Co
    final case class CustomerRegistered(customerID: String, customer: ActorRef[CustomerActor.Msg]) extends Msg
    final case class MachinesToBookmark(phMachineType: MachineTypes.MachineType, replyTo: ActorRef[CustomerActor.Msg]) extends Msg
  }

  private class GymController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) {

    val customerManager: ActorRef[CustomerManager.Msg] = context.spawn(CustomerManager(), "CustomerManager")

    override def onMessage(msg: Msg): Behavior[Msg] = {
      msg match {
        case m: Msg.DeviceInGym =>
          customerManager ! CustomerManager.RequestCustomerCreation(m.customerID, context.self, m.replyTo)
          m.replyTo ! Device.Msg.GoIdle()
          Behaviors.same

        case m: Msg.UserLogin =>  customerManager ! CustomerManager.RequestCustomerLogin(m.customerID, m.machineLabel, m.phMachineType, m.replyTo, m.pm); Behaviors.same

        case m: Msg.CustomerRegistered => childCustomerActor += ((m.customerID, m.customer)); Behaviors.same //For test purpose only

        case m: Msg.PhysicalMachineWakeUp =>
          val machine = context.spawn[MachineActor.Msg](MachineActor(context.self, m.replyTo, m.machineLabel), "MA_"+m.machineID)
          childMachineActor += (((m.machineID, m.phMachineType), machine))
          m.replyTo ! PhysicalMachine.Msg.MachineActorStarted(m.machineID, machine)
          Behaviors.same

        case m: Msg.MachinesToBookmark =>
          m.replyTo ! CustomerActor.MachineList(childMachineActor.filterKeys(k => k._2 == m.phMachineType).values.toSet)
          Behaviors.same
      }
    }
  }

  /**
   * For test purpose only
   * */
  def getChildCustomer(name: String): Option[ActorRef[CustomerActor.Msg]] = {
    childCustomerActor.get(name)
  }

  /**
   * For test purpose only
   * */
  def getChildMachineByName(name: String): Option[ActorRef[MachineActor.Msg]] = {
    childMachineActor.filterKeys(k => k._1 == name).last._2 match {
      case v: ActorRef[MachineActor.Msg] => Some(v)
      case _ => None
    }
  }
}