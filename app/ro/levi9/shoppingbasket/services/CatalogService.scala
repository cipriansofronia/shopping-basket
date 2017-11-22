package ro.levi9.shoppingbasket.services

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.Configuration
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage.ListAllItems
import ro.levi9.shoppingbasket.models.CatalogItem

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class CatalogService @Inject()(@Named("catalogActor") catalogActor: ActorRef,
                               config: Configuration)
                              (implicit ec: ExecutionContext) {

  implicit val askTimeout: Timeout = Timeout(config.get[Int]("akka.ask-timeout") seconds)

  def listCatalog: Future[List[CatalogItem]] =
    (catalogActor ? ListAllItems).mapTo[List[CatalogItem]]

}
