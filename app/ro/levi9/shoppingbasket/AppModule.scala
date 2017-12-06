package ro.levi9.shoppingbasket


import akka.actor.{ActorRef, ActorSystem, Props}
import play.api.{Configuration, Environment}
import play.api.inject._
import ro.levi9.shoppingbasket.actors.{BasketActor, CatalogActor}
import ro.levi9.shoppingbasket.services.{BasketService, CatalogService}


class AppModule extends Module {

  override def bindings(environment: Environment,
                        configuration: Configuration): Seq[Binding[_]] = {

    val actorSystem: ActorSystem = ActorSystem("shopping-basket")

    val catalogActor = actorSystem.actorOf(Props[CatalogActor])
    val basketActor = actorSystem.actorOf(Props[BasketActor])

    Seq(
      bind[ActorRef].qualifiedWith("catalogActor").toInstance(catalogActor),
      bind[ActorRef].qualifiedWith("basketActor").toInstance(basketActor),
      bind[BasketService].toSelf,
      bind[CatalogService].toSelf
    )
  }

}
