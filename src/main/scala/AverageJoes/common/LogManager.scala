package AverageJoes.common

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
  def log(s: String) = {
    println(s)
  }

  def logBehaviourChange(subject: String, behavior: String){
    log("@Behaviour %s change to %s".format(subject, behavior))
  }

  def logError(error: String): Unit ={
    log("!!!ERROR: "+error)
  }
}
