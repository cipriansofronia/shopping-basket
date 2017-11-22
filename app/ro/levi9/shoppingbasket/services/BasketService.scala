package ro.levi9.shoppingbasket.services

import javax.inject.{Inject, Named}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import play.api.Configuration
import ro.levi9.shoppingbasket.actors.messages.BasketMessage._
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage.{DecreaseStockFor, IncreaseStockFor, GetItems}
import ro.levi9.shoppingbasket.dto.BasketDTO
import ro.levi9.shoppingbasket.models.CatalogItem
import ro.levi9.shoppingbasket.services.responses.BasketServiceError.{InsufficientStockError, MissingBasketError, MissingItemError, UnexpectedMessageError}
import ro.levi9.shoppingbasket.services.responses.{BasketServiceError, BasketServiceResponse}
import ro.levi9.shoppingbasket.services.responses.BasketServiceResponse.{BasketCreatedSuccess, BasketDTOSuccess, BasketServiceSuccess}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class BasketService @Inject() (@Named("basketActor") basketActor: ActorRef,
                               @Named("catalogActor") catalogActor: ActorRef,
                               config: Configuration)
                              (implicit ec: ExecutionContext) {

  implicit val askTimeout: Timeout = Timeout(config.get[Int]("akka.ask-timeout") seconds)


  def createBasket: Future[Either[BasketServiceError, BasketServiceResponse]] =
    (basketActor ? CreateBasket).map {
      case BasketCreated(basketId) => Right(BasketCreatedSuccess(basketId))
      case _ => Left(UnexpectedMessageError)
    }

  def listBasket(basketId: String): Future[Either[BasketServiceError, BasketServiceResponse]] =
    (basketActor ? GetBasket(basketId)).flatMap {
      case BasketFound(basket) =>
        (catalogActor ? GetItems(basket.items.map(_.itemId)))
          .mapTo[Map[String, CatalogItem]]
          .map { catalog =>
            Right(BasketDTOSuccess(BasketDTO(basket, catalog)))
          }
      case BasketNotFound => Future.successful(Left(MissingBasketError))
      case _ => Future.successful(Left(UnexpectedMessageError))
    }

  def add(basketId: String, itemId: String): Future[Either[BasketServiceError, BasketServiceResponse]] =
    (basketActor ? GetBasket(basketId)).flatMap {
      case BasketFound(_) =>
        (catalogActor ? DecreaseStockFor(itemId)).flatMap {
          case CatalogMessage.ItemStockUpdated =>
            (basketActor ? AddToBasket(basketId, itemId)).map {
              case BasketItemAdded => Right(BasketServiceSuccess)
              case _ => Left(UnexpectedMessageError)
            }
          case CatalogMessage.ItemNotFound => Future.successful(Left(MissingItemError))
          case CatalogMessage.ItemOutOfStock => Future.successful(Left(InsufficientStockError))
        }
      case BasketNotFound => Future.successful(Left(MissingBasketError))
    }

  def remove(basketId: String, itemId: String): Future[Either[BasketServiceError, BasketServiceResponse]] =
    (basketActor ? DeleteFromBasket(basketId, itemId)).flatMap {
      case BasketItemRemoved =>
        (catalogActor ? IncreaseStockFor(itemId)).map {
          case CatalogMessage.ItemStockUpdated => Right(BasketServiceSuccess)
          case CatalogMessage.ItemNotFound => Left(MissingItemError)
        }
      case BasketNotFound => Future.successful(Left(MissingBasketError))
      case BasketItemNotFound => Future.successful(Left(MissingItemError))
      case _ => Future.successful(Left(UnexpectedMessageError))
    }

}
