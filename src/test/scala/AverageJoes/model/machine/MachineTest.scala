package AverageJoes.model.machine

import AverageJoes.common.MachineTypes
import AverageJoes.controller.GymController
import AverageJoes.model.hardware.PhysicalMachine
import AverageJoes.model.machine
import AverageJoes.model.machine.MachineActor.Msg
import AverageJoes.model.hardware.PhysicalMachine.Msg
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike


class MachineTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "Machine actor" must {
    val probeGym = createTestProbe[GymController.Msg]()
    val probePh = createTestProbe[PhysicalMachine.Msg]()
    val probeMA = createTestProbe[MachineActor.Msg]()
    "notify booking " in {
      val actor = spawn(MachineActor(probeGym.ref,probePh.ref," "))
      actor ! MachineActor.Msg.GoIdle("1")
      val register = probeMA.receiveMessage()
      actor ! MachineActor.Msg.UserLogIn("34","12")
    }

    /*"log the user in the machine " in {
      val actor = spawn(MachineActor(probeGym.ref,probePh.ref,MachineTypes.CHEST_FLY))

    }*/
      }

}
