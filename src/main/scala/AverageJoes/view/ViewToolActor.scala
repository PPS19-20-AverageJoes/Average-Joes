package AverageJoes.view

import AverageJoes.model.hardware
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import javax.swing.SwingUtilities

import scala.swing.GridPanel


sealed trait ViewToolActor extends AbstractBehavior[ViewToolActor.Msg] {
  val machineID: String
  createViewEntity()

  override def onMessage(msg: ViewToolActor.Msg): Behavior[ViewToolActor.Msg] = msg match {
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

  class ViewDeviceActor(override val context: ActorContext[Msg], override val machineID: String,
                        actorRef: ActorRef[Device.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor  {
    val panel: GridPanel = View._getUserView()
    var machine: UserGui = UserGui(actorRef: ActorRef[Device.Msg])

    def createViewEntity(): Unit = {
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

  class ViewPhysicalMachineActor(override val context: ActorContext[Msg],
                                 override val machineID: String,
                                 actorRef: ActorRef[PhysicalMachine.Msg])
    extends AbstractBehavior[Msg](context) with ViewToolActor {

    var panel: MachineView = View._getMachineView()
    var machine: MachineGUI = MachineGUI()

    def createViewEntity(): Unit = {
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

  object ViewPhysicalMachineActor {
    def apply(machineID:String, actorRef: ActorRef[PhysicalMachine.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewPhysicalMachineActor(context, machineID, actorRef))
  }

  object ViewDeviceActor {
    def apply(machineID:String, actorRef: ActorRef[Device.Msg]): Behavior[Msg] =
      Behaviors.setup(context => new ViewDeviceActor(context, machineID, actorRef))
  }
}
