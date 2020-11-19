package AverageJoes

import AverageJoes.common.{MachineTypes, ServerSearch}
import AverageJoes.model.hardware.HardwareController.Msg
import AverageJoes.model.hardware.{Device, HardwareController}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object HardwareApp extends App{
  HardwareTest.start()
}

object HardwareTest {//extends App {
  private val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")

  //start()

  def start() = {
    controller ! Msg.CreatePhysicalMachine("LegPress1", MachineTypes.LEG_PRESS, "LegPress A")
    controller ! Msg.CreatePhysicalMachine("ChestFly1", MachineTypes.CHEST_FLY, "ChestFly")
    controller ! Msg.CreatePhysicalMachine("LegPress2", MachineTypes.LEG_PRESS, "LegPress B")

    controller ! Msg.CreateDevice("Wristband1", Device.DeviceType.wristband)
    controller ! Msg.CreateDevice("Wristband2", Device.DeviceType.wristband)

    val test: ActorSystem[HwControllerTestMsg] = ActorSystem(HwControllerTest(controller), "GymHardware")
    test ! StartTest()
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
              println("Wristband found", w)
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