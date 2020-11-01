package AverageJoes.model.machine

import AverageJoes.controller.GymController
import AverageJoes.model.machine.PhysicalMachine.LegPress
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.{ActorRef, ActorSystem}
import org.scalatest.wordspec.AnyWordSpecLike


class MachineTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
"Machine actor" must{
  "notify booking " in {
    /*val probe = createTestProbe[MachineActor.Msg.BookingStatus]
    ActorSystem(GymController(), "GymController")
    import GymController.Msg
    val machineActor = spawn(MachineActor(ActorRef[Msg],PhysicalMachine.MachineType.legPress))
    machineActor ! MachineActor.Msg.MachineBooking("12",probe.ref)
    val response = probe.receiveMessage()
    response.status should ===(true)*/
  }

  "log the user in the machine " in {
    /*val probe = createTestProbe[MachineActor.Msg.UserLoggedInMachine]
    val machineActor = spawn(MachineActor(ActorRef[GymController.Msg],PhysicalMachine.MachineType.legPress))
    machineActor ! MachineActor.Msg.UserRef(probe.ref)
    val response = probe.receiveMessage()
    response should ===(true)*/
  }
}



}
