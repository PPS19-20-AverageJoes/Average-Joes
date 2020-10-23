package AverageJoes.common

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AbstractBehavior

trait LoggableMsg

/**
 * Convert OnMessage in OnMessageLogged for the classes that inherits
 * Implements the logging semantics for the project
 * */
trait LogOnMessage[Msg <: LoggableMsg] extends AbstractBehavior[Msg]{

  val logName: String

  override def onMessage(msg: Msg): Behavior[ Msg]={
    println(logName, msg)

    onMessageLogged(msg)
  }

  def onMessageLogged(msg: Msg): Behavior[Msg]
}


