package AverageJoes.model.customer

import AverageJoes.controller.GymController
import AverageJoes.controller.GymController.{GymController, Msg}
import AverageJoes.model.customer.CustomerManager.RequestCustomerList
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object CustomerObjectTester {
  def apply(): Behavior[Msg] = Behaviors.setup[Msg](context => {
    val customerManager: ActorRef[CustomerManager.Msg] =
      context.spawn(CustomerManager(), "customer-manager")

    val controller: ActorRef[GymController.Msg] =
      context.spawn(new GymController(context), "gym-contoller")

    customerManager ! RequestCustomerList(controller)

    GymController()
  })
}