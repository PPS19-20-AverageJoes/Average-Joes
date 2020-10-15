package AverageJoes.view

import java.awt.BorderLayout

import javax.swing.{JComponent, JFrame, JPanel, JTextArea, JTextField}

import scala.swing.Dimension

class View extends JFrame {
    setSize(new Dimension(600,400))

    val mainPanel:JPanel = new JPanel()
    mainPanel.setLayout(new BorderLayout())
    getContentPane.add(mainPanel)

    val machinePanel:JPanel = new JPanel()
    mainPanel.add(machinePanel)

    val userPanel:JPanel = new JPanel()
    userPanel.setLayout(new BorderLayout())
    mainPanel.add(userPanel, BorderLayout.WEST)
}

class MachineView(machinePanel: JPanel) extends JComponent{
    val machineData: JTextField = new JTextField("name")
    machinePanel.add(machineData)
    val display:JTextArea = new JTextArea()
    machinePanel.add(display)

    def write (msg:String): Unit ={
        display.setText(msg)
    }
}

class UserView(userPanel: JPanel) extends JComponent {
    val userData: JTextField = new JTextField("name"+"/n"+"surname")
    userPanel.add(userData)
    val wristband:JTextArea = new JTextArea()
    userPanel.add(wristband)

    def write (msg:String): Unit ={
        wristband.setText(msg)
    }
}
