package AverageJoes.model.machine
import AverageJoes.model.machine.FileWriterActor.WriteOnFile
import AverageJoes.model.workout.CyclingMachineParameters
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit


class WriteActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{
  "Write actor" must {

    "write on file" in {
      /*val fileActor = spawn(FileWriterActor())
      val probe = createTestProbe[WriteOnFile]
      fileActor ! WriteOnFile("id", CyclingMachineParameters(2,7))
      assert(probe.receiveMessage().customerID == "id")*/
    }
  }
}
