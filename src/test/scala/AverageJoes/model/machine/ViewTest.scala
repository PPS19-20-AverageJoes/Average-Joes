package AverageJoes.model.machine
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.workout.MachineTypes
import AverageJoes.view.ViewToolActor
import AverageJoes.view.ViewToolActor.{ViewDeviceActor, ViewPhysicalMachineActor}
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit

class ViewTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{
  "View tool" should {
    "create view physical update " in {
      val probePh = createTestProbe[PhysicalMachine.Msg]()
      val actor = spawn[ViewToolActor.Msg](ViewPhysicalMachineActor("m1", "m1", MachineTypes.CYCLING, probePh.ref))
      actor ! ViewToolActor.Msg.UpdateViewObject("ciao")
    }

    "create view device update " in {
      val probe = createTestProbe[Device.Msg]()
      val actor = spawn[ViewToolActor.Msg](ViewDeviceActor("", probe.ref))
      actor ! ViewToolActor.Msg.UpdateViewObject("ciao")
    }

  }

}
