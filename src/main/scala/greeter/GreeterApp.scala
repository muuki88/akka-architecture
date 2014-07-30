package greeter

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await

object GreeterApp extends App {

  println("Starting...")
  val system = ActorSystem("greetings")
  
  val service = system.actorOf(Props[GreetingActor], "greeter")
  val greeter = new GreetingConsumer(service)
  
  // Using the greeter service
  greeter.setGreeting("en", "Hello, World!")
  greeter.setGreeting("de", "Hallo, Welt!")

  implicit val timeout = Timeout(1 second)
  val en = greeter greet "en"
  val de = greeter greet "de"
  val fr = greeter greet "fr"
  
  println("English: " + Await.result(en, timeout.duration))
  println("German : " + Await.result(de, timeout.duration))
  println("French : " + Await.result(fr, timeout.duration))
  
  system.shutdown()
  println("Shutdown")
}