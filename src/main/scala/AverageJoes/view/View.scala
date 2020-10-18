import AverageJoes.model.customer.Customer

import scala.swing.{BorderPanel, Button, Component, Dimension, Frame, GridPanel, MainFrame, SimpleSwingApplication, TextArea, TextField}

object View extends SimpleSwingApplication {
    def top: Frame = new MainFrame {
        title = "AverageJoes"

        preferredSize = new Dimension(1200, 600)

        val machinePanel: MachineView = MachineView()

        val userPanel: UserView = UserView()

        contents = new BorderPanel {
            add(machinePanel, BorderPanel.Position.Center)
            add(userPanel, BorderPanel.Position.West)
        }
    }
}

case class MachineView() extends GridPanel(4,3) {

    def addUser () {
        contents += new TextField("Machine Info")
        contents += new TextArea()
    }
}

case class UserView() extends GridPanel(10,1) {

    def addUser () {
        contents += new TextField("Customer Info")
        contents += new TextArea()
    }

    contents += new Button("user")
}