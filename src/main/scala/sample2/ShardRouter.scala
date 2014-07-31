package sample2

import akka.actor._

/**
 * This singleton pattern is used to ensure only one database
 * system in the running application.
 * 
 * You could easily extend this to a database registry with different
 * [[SqlService]] actorRefs.
 */
object ShardRouter extends SqlServiceConsumer {

  private var shardManager: Option[ActorRef] = None

  /**
   * Initializes the shardRouter if not already been
   * initialialized.
   * 
   * @return an [[SqlServiceConsumer]] to query the database
   */
  def apply(system: ActorSystem): SqlServiceConsumer = {
    if (!shardManager.isDefined) {
      val actor = system.actorOf(Props[ShardRouterActor], "shards")
      shardManager = Some(actor)
    }
    this
  }

  /**
   * Returns none if the ShardRouter hasn't been initialiazed yet.
   * Call
   * 
   * {{{
   * val router = ShardRouter(system)
   * }}}
   * 
   * to initialize the system for the first time.
   * 
   * @return Some(SqlServiceConsumer) if initialized, else None
   */
  def apply(): Option[SqlServiceConsumer] = shardManager map (_ => this)

  def sqlService(): akka.actor.ActorRef =
    shardManager getOrElse (throw new IllegalStateException("initialize first with: apply(system)"))
}