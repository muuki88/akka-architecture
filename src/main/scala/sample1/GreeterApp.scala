package sample1

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await

/**
 * Small example of how to instantiate an actor system
 * and using the defined [[GreetingService]] api.
 */
object GreeterApp extends App {

  val system = ActorSystem("greetings")
  system.log.info("Greeter system started")
  
  val service = system.actorOf(Props[GreetingActor], "greeter")
  val greeter = new GreetingConsumer(service)
  
  // Using the greeter service
  greeter.setGreeting("en", "Hello, World!")
  greeter.setGreeting("de", "Hallo, Welt!")

  implicit val timeout = Timeout(1 second)
  val en = greeter greet "en"
  val de = greeter greet "de"
  val fr = greeter greet "fr"
  
  system.log.info("English: " + Await.result(en, timeout.duration))
  system.log.info("German : " + Await.result(de, timeout.duration))
  system.log.info("French : " + Await.result(fr, timeout.duration))
  
  system.log.info("Shutdown actor system")
  system.shutdown()
}