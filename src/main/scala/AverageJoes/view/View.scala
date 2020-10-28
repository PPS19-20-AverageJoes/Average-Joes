package AverageJoes.view

import scala.swing.{BorderPanel, Button, Dimension, Frame, GridPanel, MainFrame, SimpleSwingApplication, TextArea, TextField}

object View extends SimpleSwingApplication {
    val machinePanel: GridPanel = new GridPanel(3,3)
    val userPanel: GridPanel = new GridPanel(10,1)

    def top: Frame = new MainFrame {
        title = "AverageJos"

        preferredSize = new Dimension(1200, 600)

        contents = new BorderPanel {
            add(machinePanel, BorderPanel.Position.Center)
            add(userPanel, BorderPanel.Position.West)
        }
    }

    def _getMachineView(): GridPanel = machinePanel
    def _getUserView(): GridPanel = userPanel
}

case class UserGui() extends GridPanel(2,1){
    preferredSize = new Dimension(300, 600)
    contents += new Button("Customer Info")
    contents += new TextArea()

}

case class MachineGUI(val machineID: String) extends GridPanel(2,1){
    contents += new Button("Machine Info")
    contents += new TextArea()
}