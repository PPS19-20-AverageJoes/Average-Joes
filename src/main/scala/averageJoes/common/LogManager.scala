package averageJoes.common

import scala.collection.mutable.ListBuffer

trait LoggableMsg{
  log()
  def log() { LogManager.log(this.toString) }
}
trait LoggableMsgTo extends LoggableMsg{
  override def log() { LogManager.log("#To > %s | %s".format(To, this.toString)) }
  def To: String
}

trait LoggableMsgFromTo extends LoggableMsg{
  override def log() { LogManager.log("#From > %s | To > %s | %s".format(From, To, this.toString)) }
  def To: String
  def From: String
}

trait NonLoggableMsg extends LoggableMsg {
  override def log() {}
}

object LogManager{
  private var testMode: Boolean = false
  private val behaviorList: ListBuffer[(String,String)] = new ListBuffer[(String,String)]()

  def log(s: String) {
    if(!testMode) println(s)
  }

  def logBehaviourChange(subject: String, behavior: String) {
    log("@Behaviour %s change to %s".format(subject, behavior))
    if(testMode) behaviorList += ((subject,behavior))
  }

  def logError(error: String) {
    log("!!!ERROR: "+error)
  }

  def setTestMode(setMode: Boolean) { testMode = setMode }
  def getBehaviorList(): List[(String,String)] = behaviorList.toList
}
