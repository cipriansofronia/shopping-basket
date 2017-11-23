package ro.levi9.shoppingbasket

import akka.actor.{Actor, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}

class TestActorKit extends TestKit(ActorSystem("actor-system")) {

  def testActorRef[A](message: A) = TestActorRef(new Actor {
    override def receive: Receive = { case _ => sender() ! message }
  })

}
