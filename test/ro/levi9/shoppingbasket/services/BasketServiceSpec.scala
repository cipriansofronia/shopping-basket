package ro.levi9.shoppingbasket.services

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, WordSpecLike}
import play.api.Configuration
import ro.levi9.shoppingbasket.actors.messages.BasketMessage.{BasketFound, BasketItemAdded, BasketItemRemoved, BasketNotFound}
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage
import ro.levi9.shoppingbasket.dto.{BasketDTO, ItemDTO}
import ro.levi9.shoppingbasket.models.{Basket, BasketItem, CatalogItem, Item}
import ro.levi9.shoppingbasket.services.responses.BasketServiceError.MissingBasketError
import ro.levi9.shoppingbasket.services.responses.BasketServiceResponse.{BasketDTOSuccess, BasketServiceSuccess}

class BasketServiceSpec extends TestKit(ActorSystem("basket-system"))
  with MockitoSugar
  with MustMatchers
  with WordSpecLike
  with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  def testActorRef[A](message: A) = TestActorRef(new Actor {
    override def receive: Receive = { case _ => sender() ! message }
  })

  private val basketId = "demo"
  val phone = Item("ae4cd", "iPhone 7", "Apple", "phones", "phone by Steve Jobs", 3000)
  val catalogPhone = CatalogItem(phone, 10)
  val basketItemDto = ItemDTO(catalogPhone, 2)
  val basketPhoneItem = BasketItem(phone.id, 2)
  val defaultBasket = Basket(Set(basketPhoneItem))

  "A BasketService" must {
    "return all the items in the basket" in {
      val basketActorRef = testActorRef(BasketFound(defaultBasket))
      val catalogActorRef = testActorRef(Map("ae4cd" -> catalogPhone))
      val config = Configuration(ConfigFactory.load("application.conf"))
      val basketService = new BasketService(basketActorRef, catalogActorRef, config)
      val result = basketService.listBasket(basketId)

      result.futureValue mustEqual Right(BasketDTOSuccess(BasketDTO(Set(basketItemDto))))
    }

    "add an item in the basket witch doesn't exist" in {
      val basketActorRef = testActorRef(BasketNotFound)
      val catalogActorRef = testActorRef(Unit)
      val config = Configuration(ConfigFactory.load("application.conf"))
      val basketService = new BasketService(basketActorRef, catalogActorRef, config)
      val result = basketService.add(basketId, "ae4cd")

      result.futureValue mustEqual Left(MissingBasketError)
    }

    "remove an exiting item from the basket" in {
      val basketActorRef = testActorRef(BasketItemRemoved)
      val storeActorRef = testActorRef(CatalogMessage.ItemStockUpdated)
      val config = Configuration(ConfigFactory.load("application.conf"))
      val basketService = new BasketService(basketActorRef, storeActorRef, config)
      val result = basketService.remove(basketId, "ase3a")

      result.futureValue mustEqual Right(BasketServiceSuccess)
    }

  }
}
