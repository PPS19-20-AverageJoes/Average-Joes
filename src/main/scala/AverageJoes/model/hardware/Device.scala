package AverageJoes.model.hardware

import AverageJoes.common.{LogOnMessage, LoggableMsg, ServerSearch}
import AverageJoes.controller.GymController
import AverageJoes.model.machine.PhysicalMachine
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * AC
 */
trait Device extends AbstractBehavior[Device.Msg] with ServerSearch with LogOnMessage[Device.Msg]{
  val customerID: String

  //Search for the Gym Controller (the server) and send a message
  server ! GymController.Msg.DeviceInGym(customerID, context.self)

  import Device._
  override def onMessageLogged(msg: Device.Msg): Behavior[Device.Msg] = {
    msg match{
      case m: Msg.UserLoggedInMachine => display(m.machineLabel); inExercise(m.refPM)
      case m: Msg.NearDevice => rfid(m.refPM); Behaviors.same
    }
  }

  private case object TimerKey
  private def inExercise(pm: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] = Behaviors.withTimers[Msg]{ timers =>
    timers.startSingleTimer(TimerKey, HeartRateSimulation(70), 2.seconds)
    Behaviors.receiveMessage {
      case m: HeartRateSimulation =>
        val heartRate: Int = 100
        pm ! PhysicalMachine.Msg.HeartRate(heartRate)
        timers.startSingleTimer(TimerKey, HeartRateSimulation(heartRate), 2.seconds)
        Behaviors.same
    }
  }


  def display (s: String): Unit

  def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit //ToDo: convert rfid to machineCommunicationStrategy type rfid? Dovremmo utilizzare una funzione Currying?

}

object Device {

  sealed trait Msg extends LoggableMsg
  object Msg {
    final case class UserLoggedInMachine(refPM: ActorRef[PhysicalMachine.Msg], machineLabel: PhysicalMachine.MachineLabel) extends Msg
    final case class NearDevice(refPM: ActorRef[PhysicalMachine.Msg]) extends Msg
    //case class MsgDisplay(message: String) extends MsgDevice
  }
  //Self messages
  private final case class HeartRateSimulation(heartRate: Int) extends Msg

  object DeviceType extends Enumeration {
    type Type = Value
    val wristband = Value
  }

  import DeviceType._
  def apply(phMachineType: Type, deviceID: String): Behavior[Msg] = {
    phMachineType match{
      case DeviceType.wristband => Wristband(deviceID)
    }
  }

  /**
   * AC
   * @param customerID: ID of the device. In real devices, is stored on config files
   */
  class Wristband(context: ActorContext[Device.Msg], override val customerID: String) extends AbstractBehavior[Device.Msg](context) with Device {

    def display (s: String): Unit = {
      println(s)
    }

    def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit = {ref ! PhysicalMachine.Msg.Rfid(customerID)}

    override val logName: String = "Device Wristband"
    override val loggingContext: ActorContext[Device.Msg] = this.context
  }

  object Wristband{
    def apply(deviceID: String): Behavior[Device.Msg] = Behaviors.setup(context => new Wristband(context, deviceID))
  }
}