package sample2

import akka.actor._
import common.LogUnhandled
import akka.routing.BalancingPool

/**
 * This actor has to be instantiated in order to get database access.
 * It manages the underlying [[ShardDatabaseActor]]s and forwards
 * requests depending on the shard.
 * 
 * All the configuration can be done in this actor. You can simply replace
 * the [[ShardDatabaseActor]] creation with this to achieve load balancing:
 * 
 * {{{
 *  import akka.routing.BalancingPool
 *  
 *  // creates a pool of 5 ShardDatabaseActors
 *  val props = BalancingPool(5).props(Props(classOf[ShardDatabaseActor], shardId))
 *  context.actorOf(props, s"shard$shardId")
 * }}}
 * 
 * @see http://doc.akka.io/docs/akka/snapshot/scala/routing.html
 * 
 */
class ShardRouterActor extends Actor with ActorLogging with SqlService with LogUnhandled {

  import SqlService._

  /**
   * Creating a bunch of database shards
   */
  override def preStart() {
    for (shardId <- 1 to 99) {
      context.actorOf(Props(classOf[ShardDatabaseActor], shardId), s"shard$shardId")
    }
  }

  /** the composed receive block  */
  def receive = routeQueries orElse sqlReceive orElse errorOnUnhandled

  /**
   * Routing queries to the correct shard. This is like overriding protected
   * methods in a class hierarchy, except I'm overriding message handlers.
   * By composing the default $sqlReceive into the $receive method,
   * I assure that new message types will be handled, too.
   */
  def routeQueries: Receive = {
    case Query(sql, shard) => context child s"shard$shard" match {
      case Some(actor) => (actor forward Query(sql, shard))
      case None => Error(new IllegalArgumentException(s"No shard with id $shard"))
    }
  }

  /**
   * By overriding the default $sqlReceive handling, the
   * query method is useless. 
   * 
   * This has to be done with all methods which are invoked on
   * child actors that implement the same trait.
   */
  def query(sql: String, shard: Int): SqlResult =
    Error(new UnsupportedOperationException("you cannot call the query method on a shard router."))

}