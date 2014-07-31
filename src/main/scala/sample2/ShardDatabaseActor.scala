package sample2

import akka.actor._
import common.LogUnhandled
import scala.util.Random.nextLong

/**
 * Represents a connection to a database.
 * The $data field is a placeholder for a jdbc, mongo, redis, etc connection.
 * 
 * This actor can, but should not be used directly. The
 * [[ShardRouter]] singleton is responsible for managing
 * the actor hierarchy for the database system.
 */
class ShardDatabaseActor(shardId: Int) extends Actor with ActorLogging with SqlService with LogUnhandled {

  import SqlService._

  // sample database
  val data: Seq[String] = (1 to 6).map(n => (n * nextLong).toHexString)

  // fail if unknown message are sent to a database connection
  def receive = sqlReceive orElse errorOnUnhandled

  val SELECT_INDEX = "(SELECT )([\\d]*)".r

  /**
   * Dummy query implementation.
   */
  def query(sql: String, shard: Int): SqlResult = {
    require(shard == shardId, s"sent the query to the wrong shard! $shard != $shardId")
    sql match {
      case "SELECT *" => Rows(data.toStream)
      case SELECT_INDEX(_, i) => Rows(Stream(data(i.toInt)))
      case sql => Error(new UnsupportedOperationException(s"Command [$sql] not available"))
    }
  }

}