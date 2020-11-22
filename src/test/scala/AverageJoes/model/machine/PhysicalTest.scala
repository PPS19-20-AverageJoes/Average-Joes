package AverageJoes.model.machine

import AverageJoes.controller.GymController
import AverageJoes.model.hardware.PhysicalMachine
import AverageJoes.model.hardware.PhysicalMachine.Msg
import AverageJoes.model.hardware.PhysicalMachine.Msg.{MachineActorStarted, Rfid}
import AverageJoes.model.machine.MachineActor.Msg.GoIdle
import AverageJoes.model.workout.MachineTypes
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class PhysicalTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "Machine actor" must {
    val probeGym = createTestProbe[GymController.Msg]()
    val probePh = createTestProbe[PhysicalMachine.Msg]()
    val probeMA = createTestProbe[MachineActor.Msg]()
    "notify booking " in {
      val actor = spawn(PhysicalMachine("",MachineTypes.CHEST_FLY,""))
      actor ! Msg.MachineActorStarted("", probeMA.ref)
      val register = probeMA.receiveMessage()
      assert(register.isInstanceOf[GoIdle])
      actor ! Rfid("12")
      val register1 = probePh.receiveMessage()

    }


  }
}

