package greeter

import akka.actor._
import scala.concurrent.Future

/**
 * Implementation of the [[GreetingService]] trait
 */
class GreetingActor extends Actor with GreetingService with ActorLogging {

  /** language -> greeting */
  val greetings = scala.collection.mutable.Map[String, String]()

  val executionContext = scala.concurrent.ExecutionContext.global

  def receive = greetingReceive orElse {
    case msg => log warning s"Unkown message: $msg"
  }

  def greet(lang: String): scala.concurrent.Future[String] = {
    Future successful greetings.getOrElse(lang, "?")
  }

  def setGreeting(lang: String, greeting: String) = greetings.put(lang, greeting)

}