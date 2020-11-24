package averageJoes.controller

import averageJoes.common.LogManager
import averageJoes.model.hardware.{Device, HardwareController, PhysicalMachine}
import averageJoes.model.hardware.HardwareController.Msg
import averageJoes.model.hardware.PhysicalMachine.LegPressParameters
import averageJoes.model.workout.{MachineParameters, MachineTypes}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.ActorSystem
import org.scalatest.wordspec.AnyWordSpecLike

class GymControllerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  val milliSleep: Long = 2000

  "Gym controller" must{
    val controller: ActorSystem[Msg] = ActorSystem(HardwareController(), "GymHardware")
    //val gymController = GetGymController().server

    "change behaviours" in {
      val deviceName = "Wristband1"
      val machineName = "LegPress1"
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
        Thread.sleep(milliSleep*2)

        refPm ! PhysicalMachine.Msg.StartExercise(MachineParameters.extractParameters[String,Int](LegPressParameters(50,10,10,10))((ep,v) => {(ep.toString,v.toInt)}))
        Thread.sleep(milliSleep*5)

        refDevice ! Device.Msg.NearDevice(refPm)
        Thread.sleep(milliSleep*6)

        val func: (List[(String,String)], String) => List[String] = (l,logName) => l.filter(p => p._1 == logName).map(e => e._2)
        val list = LogManager.getBehaviorList()

        //println(pmLogName, func(list, pmLogName))
        //assert(func(list, pmLogName) == List("operative", "inExercise", "exerciseEnds", "operative"))
        //println(machineLogName, func(list, machineLogName))
        //assert(func(list, machineLogName) == List("idle", "connecting", "updateAndLogOut", "idle"))
      }
      else assert(false)
    }

  }

  //To access the same GymController of the HardwareController
  //case class GetGymController() extends ServerSearch
}
