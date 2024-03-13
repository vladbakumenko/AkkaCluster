import akka.actor.Address
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props}
import akka.cluster.ClusterEvent
import akka.cluster.ClusterEvent.{MemberLeft, MemberUp}
import akka.cluster.typed.{Cluster, Join}

object Main extends App {

  implicit val system: ActorSystem[MemberUp] = ActorSystem(ClusterListener.behavior, "messageListener")

  val cluster = Cluster.apply(system)

  val address = Address("tcp", "messageListener", "127.0.0.1", 2552)


  cluster.manager ! Join(address)

  val actor1 = system.systemActorOf(MessageListener.behavior, "actor1", Props.empty)

  actor1 ! MessageListener.GroupMessage("to actor1")

  println(system.address)

  println(cluster.selfMember)

  system.terminate()
}

object ClusterListener {

  def behavior: Behavior[MemberUp] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage {
      case MemberUp(member) => println("hello from" + member.address)
        Behaviors.same
    }
  }
}

object MessageListener {
  sealed trait Protocol

  case class GroupMessage(msg: String) extends Protocol
  case class PrivateMessage(msg: String, replyTo: ActorRef[String]) extends Protocol

  def behavior: Behavior[Protocol] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage {
      case GroupMessage(msg) => println(s"group msg: $msg")
        Behaviors.same
      case PrivateMessage(msg, replyTo) => println(s"prvt msg: $msg")
        replyTo ! msg
        Behaviors.same
    }
  }
}