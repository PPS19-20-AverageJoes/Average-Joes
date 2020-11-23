package AverageJoes.model.hardware

import AverageJoes.common.{LogManager, LoggableMsgTo, NonLoggableMsg, ServerSearch}
import AverageJoes.controller.GymController
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.view.ViewToolActor
import AverageJoes.view.ViewToolActor.ViewDeviceActor
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

/**
 * AC
 */
trait Device extends AbstractBehavior[Device.Msg] with ServerSearch {
  import Device._
  def customerID: String
  val deviceLabel: DeviceLabel //To show on device
  val logName: String = Device.logName +"_"+ customerID

  //Search for the Gym Controller (the server) and send a message
  server ! GymController.Msg.DeviceInGym(customerID, context.self)

  val deviceGui: ActorRef[ViewToolActor.Msg]= context.spawn[ViewToolActor.Msg](ViewDeviceActor(deviceLabel,context.self) , "D_GUI_"+customerID)

  override def onMessage(msg: Msg): Behavior[Msg] = {
    msg match{
      case Msg.GoIdle() => display(deviceLabel+" OnLine"); idle()
    }
  }

  /**
   * Idle state, the device is ready to connect
   * with a physical machine
   * */
  private def idle(): Behavior[Msg] ={
    LogManager.logBehaviourChange(logName,"idle")
    Behaviors.receiveMessagePartial {
      case m: Msg.CustomerLogged => display(m.machineLabel); waitingForStart(m.machineLabel, m.refPM) //inExercise(m.machineLabel, m.refPM)
      case m: Msg.NearDevice => rfid(m.refPM); Behaviors.same
      case HeartRateSimulation(_,_) => Behaviors.same //Ignore in this behaviour (residual)
    }
  }

  /**
   * The customer is on the machine and has to push the button to start the exercise
   * */
  private def waitingForStart(machineLabel: MachineLabel, pm: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] ={
    LogManager.logBehaviourChange(logName,"waitingForStart")
    Behaviors.receiveMessagePartial {
      case Msg.StartExercise() => inExercise(machineLabel, pm)
      case Msg.NearDevice(_) => Behaviors.same //Ignore in this behaviour
    }
  }

  private case object TimerKey
  /**
   * The customer is playing the machine
   * */
  private def inExercise(machineLabel: MachineLabel, pm: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] = Behaviors.withTimers[Msg]{ timers =>
    timers.startSingleTimer(TimerKey, HeartRateSimulation(minHeartRate, Pos()), heartRateSchedule)
    LogManager.logBehaviourChange(logName,"inExercise")
    Behaviors.receiveMessagePartial {
      case m: HeartRateSimulation =>
        val heartRateSim: (Int,Sign) = (m.heartRate, m.sign) match {
          case (hr, s: Pos) if hr < maxHeartRate => (hr + 3, s)
          case (hr, Pos()) if hr >= maxHeartRate => (hr, Neg())
          case (hr, s: Neg) if hr > minHeartRate => (hr - 1, s)
          case (hr, Neg()) if hr <= minHeartRate => (hr, Pos())
          case hr => hr
        }
        pm ! PhysicalMachine.Msg.HeartRate(heartRateSim._1)
        display(machineLabel + " HR: " + heartRateSim._1)
        timers.startSingleTimer(TimerKey, HeartRateSimulation(heartRateSim._1, heartRateSim._2), heartRateSchedule)
        Behaviors.same

      case m: Msg.NearDevice => rfid(m.refPM); Behaviors.same

      case m: Msg.CustomerLogOut=>
        m.machineLabel match {
          case some: Some[MachineLabel] => display("Booking "+some.get)
          case _ => display(deviceLabel+" OnLine")
        }
        idle()
    }
  }


  def display (s: String): Unit

  def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit

}

object Device {
  private val logName: String = "Device"
  sealed trait Msg extends LoggableMsgTo { override def To: String = "Device" }
  object Msg {
    final case class GoIdle() extends Msg
    final case class StartExercise() extends Msg
    //From GUI
    final case class NearDevice(refPM: ActorRef[PhysicalMachine.Msg]) extends Msg
    //From CustomerActor
    final case class CustomerLogged(refPM: ActorRef[PhysicalMachine.Msg], machineLabel: PhysicalMachine.MachineLabel) extends Msg
    final case class CustomerLogOut(machineLabel: Option[MachineLabel]) extends Msg
    //case class MsgDisplay(message: String) extends MsgDevice
  }
  //Self messages
  private final case class HeartRateSimulation(heartRate: Int, sign: Sign) extends Msg with NonLoggableMsg

  type DeviceLabel = String

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
  def apply(phMachineType: Type, deviceID: String, deviceLabel: DeviceLabel): Behavior[Msg] = {
    phMachineType match{
      case DeviceType.wristband => Wristband(deviceID, deviceLabel)
    }
  }

  /**
   * AC
   * @param customerID: ID of the device. In real devices, is stored on config files
   */
  class Wristband(context: ActorContext[Device.Msg], override val customerID: String, override val deviceLabel: DeviceLabel) extends AbstractBehavior[Device.Msg](context) with Device {

    //val deviceGui = context.spawn[ViewToolActor.Msg](ViewDeviceActor(customerID,context.self) , "D_GUI_"+customerID)

    def display (s: String): Unit = {
      deviceGui ! ViewToolActor.Msg.UpdateViewObject(s)
    }

    def rfid(ref: ActorRef[PhysicalMachine.Msg]) : Unit = { ref ! PhysicalMachine.Msg.Rfid(customerID) }

    //override val logName: String = "Dev_Wristband_" + customerID
  }

  object Wristband{
    def apply(customerID: String, deviceLabel: DeviceLabel): Behavior[Device.Msg] = Behaviors.setup(context => new Wristband(context, customerID, deviceLabel))
  }
}