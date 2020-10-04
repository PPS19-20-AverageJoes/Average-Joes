package AverageJoes.model.user

import akka.actor.ActorRef

trait User {
    def name: String
    def surname: String
    def CF: String
  }

