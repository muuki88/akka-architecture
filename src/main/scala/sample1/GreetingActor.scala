package sample1

import akka.actor._
import scala.concurrent.Future
import common.LogUnhandled

/**
 * Implementation of the [[GreetingService]] trait
 */
class GreetingActor extends Actor with GreetingService with ActorLogging with LogUnhandled {

  /** language -> greeting */
  val greetings = scala.collection.mutable.Map[String, String]()

  val executionContext = scala.concurrent.ExecutionContext.global

  /**
   * Wiring up the message handler
   */
  def receive = greetingReceive orElse warningOnUnhandled

  // implementation
  def greet(lang: String): scala.concurrent.Future[String] = {
    Future successful greetings.getOrElse(lang, "?")
  }

  // implementation
  def setGreeting(lang: String, greeting: String) = greetings.put(lang, greeting)

}