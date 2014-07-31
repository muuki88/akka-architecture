package sample2

import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import sample2.SqlService._

/**
 * A simple usage of the API pattern with a actor hierarchy.
 * The system looks basically like this
 * 
 * {{{
 *   /user/shards
 *   /user/shards/shard1
 *   /user/shards/shard2
 *   /user/shards/shard3
 *   ...
 * }}}
 * 
 */
object ShardApp extends App {
  
  val system = ActorSystem("database")
  system.log.info("Database system started")
  
  // 
  val database = ShardRouter(system)

  implicit val timeout = Timeout(1 second)
  val r1 = database.query("SELECT *", 1)
  val r2 = database.query("SELECT 2", 2)
  val r3 = database.query("SELECT 6", 3)
  val r4 = database.query("DELETE 2", 3)
  
  
  system.log.info(handleResult(Await.result(r1, timeout.duration)))
  system.log.info(handleResult(Await.result(r2, timeout.duration)))
  system.log.info(handleResult(Await.result(r3, timeout.duration)))
  system.log.info(handleResult(Await.result(r4, timeout.duration)))
  
  system.log.info("Shutdown actor system")
  system.shutdown()
  
  
  def handleResult(res: SqlResult): String = res match {
    case Rows(rows) => s"== Result ==\n${rows map(r => s"ROW:\t$r") mkString "\n"}"
    case Error(t) => s"ERROR[${t.getClass.getSimpleName}] :\t${t.getMessage}"
  }
}