package sample1

import akka.actor.ActorRef

class GreetingConsumer(service: ActorRef) extends GreetingServiceConsumer {
  def greetingsService() = service
}