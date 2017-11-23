package ro.levi9.shoppingbasket.services

import akka.actor.ActorRef
import akka.testkit.TestActorRef
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.Configuration
import ro.levi9.shoppingbasket.actors.BasketActor
import ro.levi9.shoppingbasket.{TestActorKit, TestValues}
import ro.levi9.shoppingbasket.actors.messages.BasketMessage._
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage
import ro.levi9.shoppingbasket.dto.{BasketDTO, UpdateItemDTO}
import ro.levi9.shoppingbasket.services.responses.BasketServiceError.{MissingBasketError, MissingItemError}
import ro.levi9.shoppingbasket.services.responses.BasketServiceResponse.{BasketCreatedSuccess, BasketDTOSuccess, BasketServiceSuccess}

class BasketServiceSpec extends TestActorKit
  with MockitoSugar
  with MustMatchers
  with WordSpecLike
  with ScalaFutures
  with TestValues {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val config = Configuration(ConfigFactory.load("application.conf"))

  private def basketService(basketActorRef: ActorRef, catalogActorRef: ActorRef): BasketService =
    new BasketService(basketActorRef, catalogActorRef, config)

  "A BasketService" must {

    "return all the items in the basket" in {
      val basketActorRef = testActorRef(BasketFound(defaultBasket))
      val catalogActorRef = testActorRef(catalogMap)
      val result = basketService(basketActorRef, catalogActorRef).listBasket(BasketHeaderValue)

      result.futureValue mustEqual Right(BasketDTOSuccess(BasketDTO(Set(basketItemDto))))
    }

    "add an item in the basket" in {
      val basketActorRef = TestActorRef(new BasketActor())
      val catalogActorRef = testActorRef(CatalogMessage.ItemStockUpdated)
      val service = basketService(basketActorRef, catalogActorRef)
      val Right(BasketCreatedSuccess(basketId)) = service.createBasket.futureValue
      val result = service.add(basketId, UpdateItemDTO(phone.id, 1))

      result.futureValue mustEqual Right(BasketServiceSuccess)
    }

    "fail to add an item in the basket which doesn't exist" in {
      val basketActorRef = testActorRef(BasketNotFound)
      val catalogActorRef = testActorRef(Unit)
      val result =
        basketService(basketActorRef, catalogActorRef)
          .add("invalid-basket-id", UpdateItemDTO(phone.id, 1))

      result.futureValue mustEqual Left(MissingBasketError)
    }

    "remove an exiting item from the basket" in {
      val basketActorRef = testActorRef(BasketItemRemoved(basketPhoneItem))
      val catalogActorRef = testActorRef(CatalogMessage.ItemStockUpdated)
      val result =
        basketService(basketActorRef, catalogActorRef)
          .remove(BasketHeaderValue, phone.id)

      result.futureValue mustEqual Right(BasketServiceSuccess)
    }

    "fail when trying to remove an item  that doesn't exist in the basket" in {
      val basketActorRef = testActorRef(BasketItemNotFound)
      val catalogActorRef = testActorRef(Unit)
      val result =
        basketService(basketActorRef, catalogActorRef)
          .remove(BasketHeaderValue, "invalid-id")

      result.futureValue mustEqual Left(MissingItemError)
    }

  }
}
