package AverageJoes.model.machine
import AverageJoes.view.ViewToolActor
import AverageJoes.view.ViewToolActor.Msg.{SetMachineParameters, UpdateViewObject}
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit

class ViewTest extends ScalaTestWithActorTestKit with AnyWordSpecLike{
  "View tool" should {

    "send display update " in {
      val probeView = createTestProbe[ViewToolActor.Msg]()
      probeView.ref ! ViewToolActor.Msg.UpdateViewObject("ciao")
      val message = probeView.receiveMessage()
      assert(message match {
        case UpdateViewObject(_) => true
        case _ => false
      })
    }

    "create view device update " in {
      val probeView = createTestProbe[ViewToolActor.Msg]()
      val map = Map.apply("DURATION"->1,"SET"->2)
      probeView.ref ! ViewToolActor.Msg.SetMachineParameters(map.toList)
      val message = probeView.receiveMessage()
      assert(message match {
        case SetMachineParameters(list) => if (list.nonEmpty) true else false
        case _ => false
      })
    }

  }

}
