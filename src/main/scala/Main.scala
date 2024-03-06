import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.cluster.typed.Cluster

object Main extends App {

  val system = ActorSystem.create(Behaviors.empty, "system")

  val cluster = Cluster(system)
}
