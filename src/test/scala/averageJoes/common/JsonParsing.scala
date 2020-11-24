package averageJoes.common

import averageJoes.model.fitness.ExecutionValues
import averageJoes.model.hardware.PhysicalMachine.CyclingMachineParameters
import averageJoes.model.machine.FileWriterActor.WriteOnFile
import averageJoes.utils.FileParser
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike


class JsonParsing extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Json parser" must {
    "to file " in {
      val path: String = "src/main/resources/workoutData.json"
      val msg = WriteOnFile("2", CyclingMachineParameters(11,56),ExecutionValues(11,34,56))
      FileParser.encoding(path, msg)
    }
  }

}
