package clusterMaybe

import akka.NotUsed
import akka.actor.Address
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.pubsub.PubSub
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.Member
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.cluster.typed.Subscribe

import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn.readLine

case class GroupMessage(value: String)

object Main {
  def apply(): Behavior[NotUsed] =
    Behaviors.setup { context =>
      Behaviors.receiveSignal { case (_, Terminated(_)) =>
        Behaviors.stopped
      }
    }

  def main(args: Array[String]): Unit = {

    val system  = ActorSystem(Main(), "system")
    val cluster = Cluster(system)

    cluster.subscriptions ! Subscribe(system.systemActorOf(Listener.apply(), "clusterListener"), classOf[ClusterDomainEvent])

    val port = readLine().toInt

    val address = Address("akka", "system", "127.0.0.1", port)

    cluster.manager ! Join(address)

    val pubSub = PubSub(system)
    val topic  = pubSub.topic[GroupMessage]("topic")
    topic ! Topic.Subscribe(system.systemActorOf(Chat(), "chat"))

    while (true) {
      val msg = readLine()
      topic ! Topic.Publish(GroupMessage(msg))
    }
  }

}

object Listener {

  val members = ArrayBuffer.empty[Member]

  def apply(): Behavior[ClusterDomainEvent] = Behaviors.receiveMessage {
    case m: MemberUp =>
      println("memberUp ==> : " + m.member.address)
      members.append(m.member)
      println("members ==> : " + members)
      Behaviors.same
    case _ => Behaviors.same
  }
}

object Chat {
  def apply(): Behavior[GroupMessage] = Behaviors.setup { context =>
    Behaviors.receiveMessage { case m: GroupMessage =>
      println("message ==> : " + m.value)
      Behaviors.same
    }
  }
}
