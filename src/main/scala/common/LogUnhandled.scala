package common

import akka.actor.{ Actor, ActorLogging }
import akka.actor.Actor.Receive

/**
 * Mixin this trait to log if an actor receives
 * a message he cannot handle. You can log on
 * different levels.
 *
 * {{{
 * import akka.actor.{ Actor, ActorLogging }
 * import akka.actor.Actor.Receive
 *
 * class MyActor extends Actor with ActorLogging with LogUnhandled {
 *
 *    def receive = myReceive orElse warningOnUnhandled
 *
 *    def myReceive : Receive = {
 *       case str: String => log info s"Handle string $str"
 *    }
 * }
 *
 * }}}
 *
 */
trait LogUnhandled { this: Actor with ActorLogging =>

  val debugOnUnhandled: Receive = {
    case msg => log debug s"Unhandled message $msg in actor ${self.path}"
  }

  val infoOnUnhandled: Receive = {
    case msg => log info s"Unhandled message $msg in actor ${self.path}"
  }

  val warningOnUnhandled: Receive = {
    case msg => log warning s"Unhandled message $msg in actor ${self.path}"
  }

  val errorgOnUnhandled: Receive = {
    case msg => log error s"Unhandled message $msg in actor ${self.path}"
  }
}

