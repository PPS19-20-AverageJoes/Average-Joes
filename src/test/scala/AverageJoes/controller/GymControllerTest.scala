package AverageJoes.controller

import AverageJoes.common.{LogManager, ServerSearch}
import AverageJoes.model.customer.CustomerActor
import AverageJoes.model.hardware.{Device, HardwareController, PhysicalMachine}
import AverageJoes.model.hardware.HardwareController.Msg
import AverageJoes.model.hardware.PhysicalMachine.LegPressParameters
import AverageJoes.model.machine.MachineActor
import AverageJoes.model.workout.{MachineParameters, MachineTypes}
import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestInbox}
import akka.actor.typed.{ActorRef, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike

class GymControllerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val milliSleep: Long = 3000

  "Gym controller" must{
    val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")
    //val gymController = GetGymController().server

    "change behaviours" in {
      val deviceName = "Wristband1"
      val machineName = "LegPress1"
      val deviceLogName = "Device_" + deviceName
      val pmLogName = "PM_" + machineName
      val machineLogName = "Machine Actor"

      LogManager.setTestMode(true)

      controller ! Msg.CreateDevice(deviceName, Device.DeviceType.wristband, deviceName)
      controller ! Msg.CreatePhysicalMachine(machineName, MachineTypes.LEG_PRESS, machineName)

      Thread.sleep(milliSleep)

      val optDevice = HardwareController.getChildDevice(deviceName)
      val optPm = HardwareController.getChildPmByName(machineName)
      //val optCustomer = GymController.getChildCustomer(deviceName)
      //val optMachine = GymController.getChildMachineByName(machineName)

      if(optDevice.nonEmpty && optPm.nonEmpty){
        val refDevice = optDevice.get
        val refPm = optPm.get

        refDevice ! Device.Msg.NearDevice(refPm)
        Thread.sleep(milliSleep)

        refPm ! PhysicalMachine.Msg.StartExercise(MachineParameters.extractParameters[String,Int](LegPressParameters(50,1,10,1))((ep,v) => {(ep.toString,v.toInt)}))
        Thread.sleep(milliSleep*3)

        refDevice ! Device.Msg.NearDevice(refPm)
        Thread.sleep(milliSleep*4)

        val func: (List[(String,String)], String) => List[String] = (l,logName) => l.filter(p => p._1 == logName).map(e => e._2)
        val list = LogManager.getBehaviorList()

        //println(deviceLogName, func(list, deviceLogName))
        assert(func(list, deviceLogName) == List("idle", "waitingForStart", "inExercise","idle"))
        //println(pmLogName, func(list, pmLogName))
        assert(func(list, pmLogName) == List("operative", "inExercise", "exerciseEnds", "operative"))
        //println(machineLogName, func(list, machineLogName))
        assert(func(list, machineLogName) == List("idle", "connecting", "updateAndLogOut", "idle"))
      }
      else assert(false)
    }


  }

  //To access the same GymController of the HardwareController
  //case class GetGymController() extends ServerSearch
}
