package sample1

import akka.actor._
import akka.actor.Actor.Receive
import akka.pattern.{ pipe, ask }
import akka.util.Timeout
import scala.concurrent.{ ExecutionContext, Future }

/**
 * A service designed to be polite in many different languages.
 * However you must learn him greetings in every language first.
 * 
 * == Sample Implementation ==
 * {{{
 * import akka.actor._
 * import scala.concurrent.Future
 * class GreetingActor extends Actor with GreetingService with ActorLogging {
 *    
 *    // the greetings dictionary  
 *    val greetings = scala.collection.mutable.Map[String, String]()
 *    
 *    val executionContext = scala.concurrent.ExecutionContext.global
 * 
 *    def receive = greetingReceive orElse {
 *      case msg => log warning ("Unkown message: " + msg)
 *    }
 *    
 *    def greet(lang: String): scala.concurrent.Future[String] = {
 *      Future successful greetings.getOrElse(lang, "?")
 *    }
 * 
 *    def setGreeting(lang: String, greeting: String) = greetings.put(lang, greeting)
 * 
 * }
 * 
 * }}}
 * 
 */
trait GreetingService { this: Actor =>

  import GreetingService._

  /** Define an execution context for Future executions */
  implicit val executionContext: ExecutionContext

  /**
   * When implementing this trait use this method to extend
   * your default receive method
   *
   * {{{
   *   def receive = echoReceive orElse {
   *     case _ => // your other implementations
   *   }
   * }}}
   *
   */
  def greetingReceive: Receive = {
    case Greet(lang) => greet(lang) pipeTo sender
    case Greeting(lang, greeting) => setGreeting(lang, greeting)
  }

  /**
   * Take your time to find the appropriate greeting
   *
   * @return the greeting
   */
  def greet(lang: String): Future[String]

  /**
   * Sets a greeting for the specified language.
   * Will override existing greetings if already
   * existend for the given language
   *
   * @param lang - language the greeting is written in
   * @param greeting
   */
  def setGreeting(lang: String, greeting: String)

}

/**
 *  == API ==
 *  Defining the API for the [[GreetingService]] trait.
 *  
 *  {{{
 *    // Message for getting a greeting in english
 *    val greet = Greet("en")
 *    
 *    // Message for setting a greeting in german
 *    val greeting = Greeting("de", "Hallo, Welt")
 *  }}}
 *  
 *  == Usage ==
 *  
 *  Use the [[GreetingServiceConsumer]] trait to get a typesafe way to
 *  access the [[GreetingSerivce]].
 *  
 *  If you want to provide your own service implement the [[GreetingSerivce]] trait.
 */
object GreetingService {

  /**
   * Send this message to receive a [[Greeting]] message
   */
  case class Greet(lang: String)

  /**
   * Send this message to set the greeting for a specified language
   */
  case class Greeting(lang: String, content: String)
}

/**
 * Implement this trait if you want access the greetings service.
 * 
 * == Sample Implementation ==
 * 
 * {{{
 * import akka.actor.ActorRef
 *
 *  class GreetingConsumer(service: ActorRef) extends GreetingServiceConsumer {
 *     def greetingsService() = service
 *  }
 *  
 * }}}
 */
trait GreetingServiceConsumer {

  import GreetingService._

  /**
   * You must provide an [[ActorRef]] to the [[GreetingService]]
   * @return actorRef - actor implementing the service API
   */
  def greetingsService(): ActorRef

  /**
   * @param lang - language you want to be greeted
   * @return the greeting
   */
  def greet(lang: String)(implicit timeout: Timeout): Future[String] = {
    (greetingsService() ? Greet(lang)).mapTo[String]
  }

  /**
   * @param lang - language the greeting is written in
   * @param greeting
   */
  def setGreeting(lang: String, greeting: String) {
    greetingsService() ! Greeting(lang, greeting)
  }
}