package AverageJoes

import AverageJoes.common.ServerSearch
import AverageJoes.model.hardware.HardwareController.Msg
import AverageJoes.model.hardware.{Device, HardwareController}
import AverageJoes.model.workout.MachineTypes
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

/**
 * For debug and testing
 * */
object HardwareApp extends App{
  HardwareTest.start(true)
}

object HardwareTest {//extends App {
  private val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")

  //start()

  def start(autostart: Boolean) = {
    controller ! Msg.CreatePhysicalMachine("LegPress1", MachineTypes.LEG_PRESS, "LegPress A")
    controller ! Msg.CreatePhysicalMachine("Cycling", MachineTypes.CYCLING, "Cycling A")
    controller ! Msg.CreatePhysicalMachine("ChestFly1", MachineTypes.CHEST_FLY, "ChestFly")
    controller ! Msg.CreatePhysicalMachine("Lifting", MachineTypes.LIFTING, "Lifting")
    controller ! Msg.CreatePhysicalMachine("Running1", MachineTypes.RUNNING, "Running A")
    controller ! Msg.CreatePhysicalMachine("Running2", MachineTypes.RUNNING, "Running B")

    controller ! Msg.CreateDevice("Wristband1", Device.DeviceType.wristband, "Wristband1")
    controller ! Msg.CreateDevice("Wristband2", Device.DeviceType.wristband, "Wristband2")
    controller ! Msg.CreateDevice("Wristband3", Device.DeviceType.wristband, "Wristband3")
    controller ! Msg.CreateDevice("Wristband4", Device.DeviceType.wristband, "Wristband4")

    val test: ActorSystem[HwControllerTestMsg] = ActorSystem(HwControllerTest(controller), "GymHardware")
    if(autostart) test ! StartTest()
  }

  trait HwControllerTestMsg
  case class StartTest() extends HwControllerTestMsg
  class HwControllerTest(context: ActorContext[HwControllerTestMsg], controller: ActorRef[HardwareController.Msg]) extends AbstractBehavior[HwControllerTestMsg](context) with ServerSearch{

    override def onMessage(msg: HwControllerTestMsg): Behavior[HwControllerTestMsg] = {
      Thread.sleep(2000)

      msg match {
        case StartTest() =>
          println("---------- START TEST ----------")
          HardwareController.getChildDevice("Wristband1") match {
            case Some(w) =>
              //println("Wristband found", w)
              HardwareController.getChildPmByName("LegPress1") match {
                case Some(l) => w ! Device.Msg.NearDevice(l)
                case None => ;
              }
            //server ! GymController.Msg.BookmarkMachines(PhysicalMachine.MachineType.legPress, w)
            case None => println("! Wristband not found!");
          }



          Behaviors.same
      }

    }
  }
  object HwControllerTest{
    def apply(controller: ActorRef[Msg]): Behavior[HwControllerTestMsg] = Behaviors.setup(context => new HwControllerTest(context, controller))
  }



}