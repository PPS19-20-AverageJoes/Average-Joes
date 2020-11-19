package AverageJoes.model.hardware

import AverageJoes.common.{LogManager, LogOnMessage, LoggableMsg, LoggableMsgTo, ServerSearch}
import AverageJoes.controller.GymController
import AverageJoes.view.ViewToolActor
import AverageJoes.view.ViewToolActor.ViewDeviceActor
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * AC
 */
trait Device extends AbstractBehavior[Device.Msg] with ServerSearch {
  def customerID: String
  val logName: String = logName +"_"+ customerID
  //Search for the Gym Controller (the server) and send a message
  server ! GymController.Msg.DeviceInGym(customerID, context.self)

  //val deviceGui = context.spawn[ViewToolActor.Msg](ViewDeviceActor(customerID,context.self) , "D_GUI_"+customerID)

  import Device._
  override def onMessage(msg: Msg): Behavior[Msg] = {
    msg match{
      case m: Msg.CustomerLogged => display(m.machineLabel); inExercise(m.refPM)
      case m: Msg.NearDevice => rfid(m.refPM); Behaviors.same
    }
  }

  private case object TimerKey
  private def inExercise(pm: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] = Behaviors.withTimers[Msg]{ timers =>
    timers.startSingleTimer(TimerKey, HeartRateSimulation(minHeartRate, Pos()), heartRateSchedule)
    LogManager.logBehaviourChange(logName,"inExercise")
    Behaviors.receiveMessage {
      case m: HeartRateSimulation =>
        val heartRateSim: (Int,Sign) = (m.heartRate, m.sign) match {
          case (hr, s: Pos) if hr < maxHeartRate => (hr + 3, s)
          case (hr, Pos()) if hr >= maxHeartRate => (hr, Neg())
          case (hr, s: Neg) if hr > minHeartRate => (hr - 1, s)
          case (hr, Neg()) if hr <= minHeartRate => (hr, Pos())
          case hr => hr
        }
        pm ! PhysicalMachine.Msg.HeartRate(heartRateSim._1)
        timers.startSingleTimer(TimerKey, HeartRateSimulation(heartRateSim._1, heartRateSim._2), heartRateSchedule)
        Behaviors.same
    }
  }


  def display (s: String): Unit

  def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit

}

object Device {
  val logName: String = "Device"
  sealed trait Msg extends LoggableMsgTo { override def To: String = "Device" }
  object Msg {
    //From GUI
    final case class NearDevice(refPM: ActorRef[PhysicalMachine.Msg]) extends Msg
    //From CustomerActor
    final case class CustomerLogged(refPM: ActorRef[PhysicalMachine.Msg], machineLabel: PhysicalMachine.MachineLabel) extends Msg
    final case class CustomerLogOut() extends Msg
    //case class MsgDisplay(message: String) extends MsgDevice
  }
  //Self messages
  private final case class HeartRateSimulation(heartRate: Int, sign: Sign) extends Msg

  private trait Sign
  private final case class Pos() extends Sign
  private final case class Neg() extends Sign
  private val minHeartRate: Int = 70
  private val maxHeartRate: Int = 130
  private val heartRateSchedule: FiniteDuration = 2.seconds

  //For further device types (new model of wristband, smartwatch, ...)
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

    //val deviceGui = context.spawn[ViewToolActor.Msg](ViewDeviceActor(context, customerID,context.self) , "DevGui_"+customerID)
    val deviceGui = context.spawn[ViewToolActor.Msg](ViewDeviceActor(customerID,context.self) , "D_GUI_"+customerID)

    def display (s: String): Unit = {
      println(logName,"[display]",s)
      deviceGui ! ViewToolActor.Msg.UpdateViewObject(s)
    }

    def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit = { ref ! PhysicalMachine.Msg.Rfid(customerID) }

    override val logName: String = "Dev_Wristband_" + customerID
  }

  object Wristband{
    def apply(customerID: String): Behavior[Device.Msg] = Behaviors.setup(context => new Wristband(context, customerID))
  }
}