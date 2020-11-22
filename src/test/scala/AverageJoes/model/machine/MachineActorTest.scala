package AverageJoes.model.machine

import AverageJoes.model.customer.{CustomerActor, MachineBooker}
import AverageJoes.model.fitness.Exercise
import AverageJoes.model.hardware.PhysicalMachine.CyclingMachineParameters
import AverageJoes.model.machine.MachineActor.Msg.{BookingRequest, CustomerLogging, UserLogIn}
import AverageJoes.model.workout.MachineTypes
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike


class MachineActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "Machine actor" must {
    val probeMA = createTestProbe[MachineActor.Msg]()
    val probeBk = createTestProbe[MachineBooker.Msg]()
    val probeCustomer = createTestProbe[CustomerActor.Msg]()

    "notify log" in {
      probeMA.ref ! MachineActor.Msg.UserLogIn("Wristband2", "CHEST_FLY", MachineTypes.CHEST_FLY)
      val message = probeMA.receiveMessage()
      assert(message match {
        case UserLogIn(_, _, _) => true
        case _ => false
      })
    }

    "receive booking request " in {
      probeMA.ref ! MachineActor.Msg.BookingRequest(probeBk.ref,"Wristband2")
      val message = probeMA.receiveMessage()
      assert(message match {
        case BookingRequest(_, _) => true
        case _ => false
      })
    }

    "receive customer logging " in {
      probeMA.ref ! MachineActor.Msg.CustomerLogging("Wristaband2", probeCustomer.ref,
        Option.apply(Exercise.apply(1,CyclingMachineParameters(2,3))),isLogged = true)
      val message = probeMA.receiveMessage()
      assert(message match {
        case CustomerLogging(_, _, _, _) => true
        case _ => false
      })
    }

  }

}
