package AverageJoes

import AverageJoes.controller.HardwareController
import AverageJoes.model.device.Device
import AverageJoes.model.machine.PhysicalMachine
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}

object HardwareApp extends App{
  private val controller: ActorSystem[HardwareController.Msg] = ActorSystem(HardwareController(), "GymHardware")

  controller ! HardwareController.Msg.CreatePhysicalMachine("LegPress1",PhysicalMachine.MachineType.legPress)
  controller ! HardwareController.Msg.CreatePhysicalMachine("ChestFly1",PhysicalMachine.MachineType.chestFly)

  controller ! HardwareController.Msg.CreateDevice("Wristband1", Device.DeviceType.wristband)
  controller ! HardwareController.Msg.CreateDevice("Wristband2", Device.DeviceType.wristband)

  private val test: ActorSystem[HwControllerTestMsg] = ActorSystem(HwControllerTest(controller), "GymHardware")
  test ! StartTest()

  trait HwControllerTestMsg
  case class StartTest() extends HwControllerTestMsg
  class HwControllerTest(context: ActorContext[HwControllerTestMsg], controller: ActorRef[HardwareController.Msg]) extends AbstractBehavior[HwControllerTestMsg](context){

    override def onMessage(msg: HwControllerTestMsg): Behavior[HwControllerTestMsg] = {
      Thread.sleep(2000)

      msg match {
        case StartTest() =>
          HardwareController.getChildDevice("Wristband1") match {
            case Some(w) =>
              print("Wristband found", w)
              HardwareController.getChildPmByName("LegPress1") match {
                case Some(l) => w ! Device.Msg.NearDevice(l)
                case None => ;
              }
            case None => print("Wristband not found");
          }

          Behaviors.same
      }

    }
  }
  object HwControllerTest{
    def apply(controller: ActorRef[HardwareController.Msg]): Behavior[HwControllerTestMsg] = Behaviors.setup(context => new HwControllerTest(context, controller))
  }



}
