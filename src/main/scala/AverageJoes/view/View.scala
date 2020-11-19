package AverageJoes.view

import AverageJoes.HardwareApp
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import akka.actor.typed.ActorRef

import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, Button, Dialog, Dimension, Frame, GridPanel, MainFrame, SimpleSwingApplication, TextArea}

object View extends SimpleSwingApplication {
    private val machinePanel: MachineView = MachineView()
    private val userPanel: UserView = UserView()

    def top: Frame = new MainFrame {
        title = "AverageJoe's"

        preferredSize = new Dimension(1200, 600)

        contents = new BorderPanel {
            add(machinePanel, BorderPanel.Position.Center)
            add(userPanel, BorderPanel.Position.West)
        }

        HardwareApp.start()
    }

    def _getMachineView(): MachineView = machinePanel
    def _getUserView(): GridPanel = userPanel
}

case class UserView() extends GridPanel(10,1){
    //contents += UserGui()
}

case class MachineView() extends GridPanel(3,3){
    private var map:Map[String, ActorRef[PhysicalMachine.Msg]] = Map.empty
    //contents += MachineGUI()

    def addEntry(name:String, actorRef: ActorRef[PhysicalMachine.Msg]):Unit =
        map += (name -> actorRef)

    def _getMapKeyList(): List[String] = map.keySet.toList

    def _getMapValue(key:String): Option[ActorRef[PhysicalMachine.Msg]] = map.get(key)
}

case class UserGui(deviceActor: ActorRef[Device.Msg]) extends GridPanel(2,1){
    preferredSize = new Dimension(300, 600)
    val button:Button = new Button("Customer Info")
    var text:TextArea = new TextArea()
    var physicalActor: Option[ActorRef[PhysicalMachine.Msg]] = None
    contents += button
    contents += text
    listenTo(button)

    reactions += {
        case ButtonClicked(button) =>
            val machineChoice = Dialog.showInput(contents.head,"Choose Machine","",
                entries = View._getMachineView()._getMapKeyList(), initial = 0)
            if(machineChoice.get != None){
                physicalActor = View._getMachineView()._getMapValue(machineChoice.get.asInstanceOf[String])
                /*Send physical machine path to device*/
                deviceActor ! Device.Msg.NearDevice(physicalActor.get)
            }
    }

    def update (msg: String): Unit = {
        text.text = msg
    }
}

case class MachineGUI() extends GridPanel(2,1){
    val button:Button = new Button("Machine Info")
    var text:TextArea = new TextArea()
    contents += button
    contents += text
    listenTo(button)

    reactions +={
        case ButtonClicked(button) => ???
    }

    def update (msg: String): Unit = {
        text.text = msg
    }

}
