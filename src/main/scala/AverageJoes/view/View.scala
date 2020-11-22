package AverageJoes.view

import AverageJoes.HardwareTest
import AverageJoes.model.workout.MachineTypes.{CYCLING, MachineType}
import AverageJoes.model.hardware.PhysicalMachine.MachineLabel
import AverageJoes.model.hardware.{Device, PhysicalMachine}
import AverageJoes.model.workout.MachineTypeConverters
import akka.actor.typed.ActorRef

import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, Button, Dialog, Dimension, FlowPanel, Frame, GridPanel, Label, MainFrame, SimpleSwingApplication, Swing, TextArea, TextField}

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

        Thread.sleep(1000)
        HardwareTest.start(false)
    }

    def _getMachineView(): MachineView = machinePanel
    def _getUserView(): GridPanel = userPanel
}

case class UserView() extends GridPanel(10,1){
   //contents += UserGuiProva()
}

case class MachineView() extends GridPanel(3,3){
    private var map:Map[String, ActorRef[PhysicalMachine.Msg]] = Map.empty
   //contents += MachineGUI(machineLabel = "label",machineType = CYCLING)

    def addEntry(name:String, actorRef: ActorRef[PhysicalMachine.Msg]):Unit =
        map += (name -> actorRef)

    def _getMapKeyList(): List[String] = map.keySet.toList

    def _getMapValue(key:String): Option[ActorRef[PhysicalMachine.Msg]] = map.get(key)
}

/*case class UserGuiProva() extends GridPanel(2,1) {
    preferredSize = new Dimension(300, 600)
    private val button:Button = new Button("device")
    private var text:TextArea = new TextArea()
    contents += button
    contents += text
}*/

case class UserGui(deviceActor: ActorRef[Device.Msg], deviceLabel: String) extends GridPanel(2,1){
    preferredSize = new Dimension(300, 600)
    private val button:Button = new Button(deviceLabel)
    private val text:TextArea = new TextArea()
    private var physicalActor: Option[ActorRef[PhysicalMachine.Msg]] = None
    contents += button
    contents += text
    listenTo(button)

    reactions += {
        case ButtonClicked(_) =>
            val machineChoice = Dialog.showInput(contents.head,"Choose Machine","",
                entries = View._getMachineView()._getMapKeyList(), initial = 0)
            machineChoice match {
                case Some(_) =>  physicalActor = View._getMachineView()._getMapValue(machineChoice.get.asInstanceOf[String])
                    /*Send physical machine path to device*/
                    println("!!!!!!!!!!!!!"+"deviceActor: %s, physicalm: %s".format(deviceActor,physicalActor))
                    deviceActor ! Device.Msg.NearDevice(physicalActor.get)
                case None =>
            }

    }

    def update (msg: String): Unit = {
        text.text = msg
    }
}

case class MachineGUI(machineLabel: MachineLabel, machineType: MachineType, actorRef: ActorRef[PhysicalMachine.Msg]) extends GridPanel(3,1){
    private val label: Label = new Label(machineLabel)
    private val start:Button = new Button("START")
    private val text:TextArea = new TextArea()
    private var map:Map[String, TextField] = Map.empty

    contents += new GridPanel(1,2){
        contents += label
        contents += start
    }

    contents += new GridPanel(1,4) {
        println("View, machineType: "+machineType)
        MachineTypeConverters.setParametersView(machineType).foreach(x => {
            val label = new Label(x){
                preferredSize = new Dimension(100,20)
            }
            val textParameter = new TextField(){
                preferredSize = new Dimension(100,20)
            }
            contents += new FlowPanel(){
                contents += label
                contents += textParameter
            }
            map += (label.text -> textParameter)
        })
    }
    contents += text
    listenTo(start)

    reactions +={
        case ButtonClicked(_) =>
            try {
                if(getParameters != Nil){
                    actorRef ! PhysicalMachine.Msg.StartExercise(getParameters)
                    setButton(false)
                }
            } catch {
                case _: NumberFormatException =>
                    Dialog.showConfirmation(contents.head,
                        "Insert correct value",
                        optionType=Dialog.Options.Default,
                        title="INPUT ERROR")
            }
    }

     def update (msg: String): Unit = {
        text.text = msg
    }

    def setParameters(listParameters: List[(String,Int)]): Unit = {
        listParameters.foreach(x => {
            map(x._1).text = x._2.toString
        })
    }

     def getParameters: List[(String, Int)]  = {
        var tmp: Map[String, Int] = Map.empty
        map foreach(x => {
            if(x._2.text != "" && x._2.text.length < 4 && x._2.text.toInt != 0){
                tmp += (x._1 -> x._2.text.toInt)
            } else {
                throw new NumberFormatException
            }
        })
        tmp.toList
    }

    def setButton(boolean: Boolean): Unit = {
        start.enabled = boolean
    }

    def clearFields(): Unit = {
        map foreach(x => x._2.text = "")
    }

}
