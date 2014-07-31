package sample2

import akka.actor._
import akka.actor.Actor.Receive
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Future

/**
 * This service let's you submit [[Query]]s and get an answer
 * from a database. The database is determined by a shardId.
 * 
 * == Implementation Guidance ==
 * 
 * If you have only one database you ignore the shardId parameter.
 * If you have a sharded database system, implement a router
 * which decided to which shard the query should be forwarded.
 * 
 */
trait SqlService { this: Actor =>

  import SqlService._
  
  val sqlReceive: Receive = {
    case Query(sql, shard) => try {
     sender ! query(sql, shard) 
    } catch {
      case t: Throwable => sender ! Error(t)
    }
  }

  /**
   * Fires a query to the database an return the
   * result of this query
   */
  def query(sql: String, shard: Int): SqlResult
  
}

/**
 * The service API contains a [[Query]] message class
 * and a [[SqlResult]] trait, which has to be matched.
 * 
 * {{{
 *   sqlResult match {
 *      case Rows(rows) => rows foreach println
 *      case Error(t) = log error ("Query failed", t)
 *   }
 * }}}
 */
object SqlService {

  case class Query(sql: String, shard: Int)
  
  sealed trait SqlResult
  
  case class Rows(rows: Stream[String]) extends SqlResult
  case class Error(t: Throwable) extends SqlResult
}

/**
 * Implement this if you want to be able to send queries
 * to a database system or instantiate a class implement
 * this trait.
 * 
 */
trait SqlServiceConsumer {

  import SqlService._

  /**
   * An actor implementing the [[SqlService]] trait.
   */
  def sqlService(): ActorRef

  /**
   * Sends a query to the sql service of this consumer.
   */
  def query(sql: String, shard: Int)(implicit timeout: Timeout): Future[SqlResult] = {
    (sqlService() ? Query(sql, shard)).mapTo[SqlResult]
  }

}