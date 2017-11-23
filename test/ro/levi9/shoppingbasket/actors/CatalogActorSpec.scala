package ro.levi9.shoppingbasket.actors

import akka.pattern.ask
import akka.testkit.TestActorRef
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage._
import ro.levi9.shoppingbasket.dto.UpdateItemDTO
import ro.levi9.shoppingbasket.models.BasketItem
import ro.levi9.shoppingbasket.{TestActorKit, TestValues}

import scala.concurrent.duration._

class CatalogActorSpec extends TestActorKit
  with WordSpecLike
  with MustMatchers
  with ScalaFutures
  with MockitoSugar
  with TestValues {

  implicit val askTimeout = Timeout(2.seconds)

  val catalogActorRef = TestActorRef(new CatalogActor())

  "A CatalogActor" must {

    "get an item from catalog" in {
     val result = (catalogActorRef ? GetItems(Set("id123"))).futureValue
     result mustEqual catalogMap
    }

    "decrease stock for an item in catalog" in {
      val result = (catalogActorRef ? DecreaseStockFor(UpdateItemDTO(phone.id, 2))).futureValue
      result mustEqual ItemStockUpdated
    }

    "fail when it tries to decrease an out of stock item" in {
      val result = (catalogActorRef ? DecreaseStockFor(UpdateItemDTO(phone.id, 1))).futureValue
      result mustEqual ItemOutOfStock
    }

    "fail when it tries to decrease an item that doesn't exist" in {
      val result = (catalogActorRef ? DecreaseStockFor(UpdateItemDTO("invalid-id", 1))).futureValue
      result mustEqual ItemNotFound
    }

    "increase stock for an item" in {
      val result = (catalogActorRef ? IncreaseStockFor(BasketItem(phone.id, 2))).futureValue
      result mustEqual ItemStockUpdated
    }

    "fail when increasing the stock for an item that doesn't exist" in {
      val result = (catalogActorRef ? IncreaseStockFor(BasketItem("invalid-id", 2))).futureValue
      result mustEqual ItemNotFound
    }

  }
}
