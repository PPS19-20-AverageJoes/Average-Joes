package AverageJoes.view

import AverageJoes.model.hardware.Device
import AverageJoes.model.machine.PhysicalMachine
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javax.swing.SwingUtilities

import scala.swing.GridPanel


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
  val machineID: String
  var machine: GridPanel

  override def onMessage(msg: ViewToolActor.Msg): Behavior[ViewToolActor.Msg] = msg match {
    case m: ViewToolActor.Msg.CreateViewObject => createViewEntity(); Behaviors.same
    case m: ViewToolActor.Msg.UpdateViewObject => updateViewEntity(); Behaviors.same

  }
  def createViewEntity()
  def updateViewEntity()
}

object ViewToolActor {
  sealed trait Msg

  object Msg {
    final case class CreateViewObject() extends Msg
    final case class UpdateViewObject() extends Msg
  }

  case class ViewDeviceActor(override val context: ActorContext[Msg],
                             panel: UserView, override val machineID: String,
                             actorRef: ActorRef[Device.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor  {

    override var machine: GridPanel = UserGui(/*actorRef: ActorRef[Device.Msg]*/)

    override def createViewEntity(): Unit = {
      SwingUtilities.invokeLater(() => {
        panel.contents += machine
      })
    }

    override def updateViewEntity(): Unit = ???
  }

  case class ViewPhysicalMachineActor(override val context: ActorContext[Msg],
                                       panel: MachineView, override val machineID: String,
                                      actorRef: ActorRef[PhysicalMachine.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor {

    override var machine: GridPanel = MachineGUI()

    override def createViewEntity(): Unit = {
      SwingUtilities.invokeLater(() => {
        panel.contents += machine
        panel.addEntry(machineID, actorRef)
      })

    }

    override def updateViewEntity(): Unit = ???
  }
}
