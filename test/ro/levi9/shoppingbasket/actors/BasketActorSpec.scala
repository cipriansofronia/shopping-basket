package ro.levi9.shoppingbasket.actors

import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}
import ro.levi9.shoppingbasket.actors.messages.BasketMessage._
import ro.levi9.shoppingbasket.models.Basket
import ro.levi9.shoppingbasket.{TestActorKit, TestValues}

import scala.concurrent.duration._

class BasketActorSpec extends TestActorKit
  with WordSpecLike
  with MustMatchers
  with ScalaFutures
  with TestValues
  with MockitoSugar {

  implicit val askTimeout = Timeout(2.seconds)

  val basketActorRef = TestActorRef(new BasketActor())
  val BasketCreated(basketId) =  (basketActorRef ? CreateBasket).futureValue

  "A BasketActor" must {

    "get an empty basket" in {
     val result = (basketActorRef ? GetBasket(basketId)).futureValue
     result mustEqual BasketFound(Basket(Set()))
    }

    "add an item to a specific basket" in {
      val result = (basketActorRef ? AddToBasket(basketId, catalogItemDto)).futureValue
      result mustEqual BasketItemAdded
    }

    "fail when tring to add an item to a non existing basket" in {
      val result = (basketActorRef ? AddToBasket("invalid-basket-id", catalogItemDto)).futureValue
      result mustEqual BasketNotFound
    }

    "remove an item from a specific basket" in {
      val result = (basketActorRef ? DeleteFromBasket(basketId, catalogItemDto.itemId)).futureValue
      result mustEqual BasketItemRemoved(basketPhoneItem)
    }

    "fail when trying to remove an item that no longer exists from a specific basket" in {
      val result = (basketActorRef ? DeleteFromBasket(basketId, catalogItemDto.itemId)).futureValue
      result mustEqual BasketItemNotFound
    }

    "fail when trying to remove an item from a non existing basket" in {
      val result = (basketActorRef ? DeleteFromBasket("invalid-basket-id", catalogItemDto.itemId)).futureValue
      result mustEqual BasketNotFound
    }

  }
}
