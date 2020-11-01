package AverageJoes.controller

import AverageJoes.common.{LogOnMessage, LoggableMsg}
import AverageJoes.model.customer.CustomerManager
import AverageJoes.model.device.Device
import AverageJoes.model.machine
import AverageJoes.model.machine.{MachineActor, PhysicalMachine}
import AverageJoes.model.workout.MachineParameters
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}

import scala.collection.mutable

object GymController {
  def apply(): Behavior[Msg] = Behaviors.setup(context => new GymController(context))

  private var childUserActor = mutable.Map.empty[String, ActorRef[CustomerManager.Command]] //Child User
  private var childMachineActor = mutable.Map.empty[(String, PhysicalMachine.MachineType.Type), ActorRef[MachineActor.Msg]] //Child Machines

  sealed trait Msg extends LoggableMsg
  object Msg{
    final case class DeviceInGym(deviceID: String, replyTo: ActorRef[Device.Msg]) extends Msg //Device enter in Gym
    final case class UserLogin(userID: String, replyTo:ActorRef[MachineActor.Msg]) extends Msg //User logged
    final case class PhysicalMachineWakeUp(machineID: String, phMachineType: PhysicalMachine.MachineType.Type, replyTo: ActorRef[HardwareController.Msg]) extends Msg //Login to the controller

    //ToDo: temporaneamente modificato  il replyto per i test, deve essere un CustomerManager.Command
    final case class BookmarkMachines(phMachineType: PhysicalMachine.MachineType.Type, replyTo: ActorRef[Device.Msg]) extends Msg

    final case class UserMachineWorkoutPlan(userID: String, exercise: Class[_ <: MachineParameters]) extends Msg
    final case class UserMachineWorkoutCompleted(user: ActorRef[MachineActor.Msg], exercise: Class[_ <: MachineParameters]) extends Msg
    final case class UserLogInStatus(status: Boolean) extends Msg
  }

  class GymController(context: ActorContext[Msg]) extends AbstractBehavior[Msg](context) with LogOnMessage[Msg]{
    override val logName = "Gym controller"
    override val loggingContext: ActorContext[Msg] = this.context

    override def onMessageLogged(msg: Msg): Behavior[Msg] = {
      msg match {
        case m: Msg.DeviceInGym =>
          val userID = m.deviceID //ToDo: recuperare utente relativo a device
          //val user = context.actorOf(Props(new SmartGymUserImpl("", "", "", "")), "actorUser") //ToDo: call constructor form user object
          //childUserActor += ((userID, user))
          //m.replyTo ! Device.Msg.UserRef(user)
          Behaviors.same
        /*case m: Msg.UserLogin =>
          val user = childUserActor(m.userID) //ToDo: optional, gestire
          m.replyTo ! MachineActor.Msg.MsgUserRef(user)
        */
        case m: Msg.PhysicalMachineWakeUp =>
          val machine = context.spawn[MachineActor.Msg](MachineActor(context.self, m.phMachineType), "MA_"+m.machineID)
          childMachineActor += (((m.machineID, m.phMachineType), machine))
          m.replyTo ! HardwareController.Msg.MachineActorStarted(m.machineID, m.phMachineType, machine)
          Behaviors.same

        case m: Msg.BookmarkMachines =>
          val children = getChildrenMachinesByType(m.phMachineType).toIterator
          if (children.nonEmpty) for (elem <- children) elem ! MachineActor.Msg.BookingRequest(m.replyTo)


          Behaviors.same
      }
    }
  }


  def getChildrenMachinesByType(pmType: PhysicalMachine.MachineType.Type): Iterable[ActorRef[machine.MachineActor.Msg]] = {
    childMachineActor.filterKeys(k => k._2 == pmType).values
  }


}