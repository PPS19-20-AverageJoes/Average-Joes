package AverageJoes.model.user

import akka.actor.ActorRef

trait User {
    def name: String
    def surname: String
    def CF: String
  }

  trait SmartGymUser extends User {
    def userID: String
    def isLogged: Boolean
    def logIn(to: ActorRef): Unit
    def logOut(): Unit
  }