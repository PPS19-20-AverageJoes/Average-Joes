package AverageJoes.view

import AverageJoes.model.device.Device
import AverageJoes.model.machine.PhysicalMachine
import AverageJoes.view.ViewToolActor.Msg
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javax.swing.SwingUtilities

import scala.swing.GridPanel


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
  val machineID: String
  var machine: GridPanel

  override def onMessage(msg: ViewToolActor.Msg): Behavior[ViewToolActor.Msg] = msg match {
    case m: ViewToolActor.Msg.CreateViewObject => createViewEntity(); Behaviors.same
    case m: ViewToolActor.Msg.UpdateViewObject => updateViewEntity(m.msg); Behaviors.same

  }
  def createViewEntity()
  def updateViewEntity(mg: String)
}

object ViewToolActor {
  sealed trait Msg

  object Msg {
    final case class CreateViewObject() extends Msg
    final case class UpdateViewObject(msg: String) extends Msg
  }

  case class ViewDeviceActor(override val context: ActorContext[Msg],
                             panel: UserView, override val machineID: String,
                             actorRef: ActorRef[Device.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor  {

    override var machine: UserGui = UserGui(actorRef: ActorRef[Device.Msg])

    override def createViewEntity(): Unit = {
      SwingUtilities.invokeLater(() => {
        panel.contents += machine
      })
    }

    override def updateViewEntity(msg: String): Unit = {
      SwingUtilities.invokeAndWait(()=>{
        machine.update(msg)
      })
    }
  }

  case class ViewPhysicalMachineActor(override val context: ActorContext[Msg],
                                       panel: MachineView, override val machineID: String,
                                      actorRef: ActorRef[PhysicalMachine.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor {

    override var machine: MachineGUI = MachineGUI()

    override def createViewEntity(): Unit = {
      SwingUtilities.invokeLater(() => {
        panel.contents += machine
        panel.addEntry(machineID, actorRef)
      })

    }

    override def updateViewEntity(msg: String): Unit = {
      SwingUtilities.invokeAndWait(()=>{
         machine.update(msg)
      })
    }
  }
}
