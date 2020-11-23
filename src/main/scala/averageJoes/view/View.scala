package averageJoes.view

import averageJoes.HardwareTest
import averageJoes.common.{ControllerSearch, ServerSearch}
import averageJoes.model.hardware.HardwareController.Msg
import averageJoes.model.workout.MachineTypes.MachineType
import averageJoes.model.hardware.PhysicalMachine.MachineLabel
import averageJoes.model.hardware.{Device, HardwareController, PhysicalMachine}
import averageJoes.model.workout.{MachineTypeConverters, MachineTypes}
import akka.actor.typed.{ActorRef, ActorSystem}
import javax.swing.WindowConstants

import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, Button, Dialog, Dimension, FlowPanel, GridPanel, Label, MainFrame, SimpleSwingApplication, TextArea, TextField}

object View extends SimpleSwingApplication {
    private val machinePanel: MachineView = MachineView()
    private val userPanel: UserView = UserView()
    def top: MainFrame = new MainFrame {
        title = "AverageJoe's"
        peer.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

        preferredSize = new Dimension(1200, 600)

        contents = new BorderPanel {
            add(machinePanel, BorderPanel.Position.Center)
            add(userPanel, BorderPanel.Position.West)
        }

        Thread.sleep(1000)
        viewInit()

        override def closeOperation(): Unit = {
            top.close()
            ServerSearch.closeSystem()
            ControllerSearch.closeSystem()
        }
    }

    def _getMachineView(): MachineView = machinePanel
    def _getUserView(): GridPanel = userPanel

    def viewInit(): Unit = {
        val controller = ControllerSearch.getController
        controller ! Msg.CreatePhysicalMachine("LegPress1", MachineTypes.LEG_PRESS, "LegPress A")
        controller ! Msg.CreatePhysicalMachine("Cycling", MachineTypes.CYCLING, "Cycling A")
        controller ! Msg.CreatePhysicalMachine("ChestFly1", MachineTypes.CHEST_FLY, "ChestFly")
        controller ! Msg.CreatePhysicalMachine("Lifting", MachineTypes.LIFTING, "Lifting")
        controller ! Msg.CreatePhysicalMachine("Running1", MachineTypes.RUNNING, "Running A")
        controller ! Msg.CreatePhysicalMachine("Running2", MachineTypes.RUNNING, "Running B")

        controller ! Msg.CreateDevice("Wristband1", Device.DeviceType.wristband, "Wristband1")
        controller ! Msg.CreateDevice("Wristband2", Device.DeviceType.wristband, "Wristband2")
        controller ! Msg.CreateDevice("Wristband3", Device.DeviceType.wristband, "Wristband3")
        controller ! Msg.CreateDevice("Wristband4", Device.DeviceType.wristband, "Wristband4")
    }
}

case class UserView() extends GridPanel(10,1){

}

case class MachineView() extends GridPanel(3,3){
    private var map:Map[String, ActorRef[PhysicalMachine.Msg]] = Map.empty

    def addEntry(name:String, actorRef: ActorRef[PhysicalMachine.Msg]):Unit =
        map += (name -> actorRef)

    def _getMapKeyList(): List[String] = map.keySet.toList

    def _getMapValue(key:String): Option[ActorRef[PhysicalMachine.Msg]] = map.get(key)
}


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
                    setButton(true)
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
