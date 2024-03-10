import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.cluster.typed.Cluster

object Main extends App {
  implicit val system: ActorSystem[MessageListener.Protocol] = ActorSystem(MessageListener.behavior, "messageListener")

  system ! MessageListener.GroupMessage("123")

  system.terminate()
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