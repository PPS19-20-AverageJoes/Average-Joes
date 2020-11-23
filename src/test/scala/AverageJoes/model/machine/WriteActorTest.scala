package AverageJoes.model.machine
import AverageJoes.model.fitness.ExecutionValues
import AverageJoes.model.hardware.PhysicalMachine.CyclingMachineParameters
import AverageJoes.model.machine.FileWriterActor.WriteOnFile
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit


class WriteActorTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{
  "Write actor" must {
    "write on file" in {
      val probe = createTestProbe[FileWriterActor.Msg]
      probe.ref ! WriteOnFile("id", CyclingMachineParameters(2,7),ExecutionValues(23, 44, 45))
      assert(probe.receiveMessage() match {
        case WriteOnFile(_, _, _) => true
        case _ => false
      })
    }

    "check parameters not empty" in {
      val probe = createTestProbe[FileWriterActor.Msg]
      probe.ref ! WriteOnFile("id", CyclingMachineParameters(2,7),ExecutionValues(23, 44, 45))
      probe.receiveMessage() match {
        case WriteOnFile(customerID, machineParameters, executionValues) => assert(customerID.equals("id"))
        case _ => false
      }
    }

  }
}
