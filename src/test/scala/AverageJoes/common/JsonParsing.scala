package AverageJoes.common

import AverageJoes.model.hardware.PhysicalMachine.CyclingMachineParameters
import AverageJoes.model.machine.FileWriterActor.WriteOnFile
import AverageJoes.utils.FileParser
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike


class JsonParsing extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Json parser" must {
    "to file " in {
      val path: String = "src/main/resources/workoutData.json"
      val msg = WriteOnFile("2", CyclingMachineParameters(11,56))
      FileParser.encoding(path, msg)
    }
  }

}
