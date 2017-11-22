package ro.levi9.shoppingbasket.controllers

import akka.stream.Materializer
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import ro.levi9.shoppingbasket.services.responses.BasketServiceError.{InsufficientStockError, MissingItemError}
import ro.levi9.shoppingbasket.services.responses.BasketServiceResponse.BasketServiceSuccess
import ro.levi9.shoppingbasket.services.{BasketService, CatalogService}

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class MainControllerSpec extends PlaySpec with MockitoSugar with GuiceOneAppPerSuite {

  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit lazy val materializer: Materializer = app.materializer

  private val basketId = "demo-id"
  private val BasketHeaderId = "X-Basket-Id"

  private val mockBasketService = mock[BasketService]
  private val mockCatalogService = mock[CatalogService]

  private val controller = new GuiceApplicationBuilder()
    .configure(Map("play.filters.headers.x-basket-id" -> BasketHeaderId))
    .configure("akka.ask-timeout" -> 2)
    .overrides(bind[BasketService].toInstance(mockBasketService))
    .overrides(bind[CatalogService].toInstance(mockCatalogService))
    .build()
    .injector
    .instanceOf[MainController]


  "A MainController" should {

    "return a list of items in the catalog" in {
      when(mockCatalogService.listCatalog) thenReturn Future(List())

      val result = controller.listCatalog(FakeRequest(GET, "/shoppingbasket/catalog"))
      assert(status(result) ==  OK)
    }

    "successfully add an existing item if the stock is sufficient" in {
      when(mockBasketService.add(any[String], any[String])) thenReturn Future(Right(BasketServiceSuccess))

      val req = FakeRequest(POST, "/shoppingbasket/update/id123")
        .withHeaders(BasketHeaderId -> basketId)

      val result = call(controller.addItemToBasket(basketId), req)
      assert(status(result) == CREATED)
    }


    "inform the user that there is not enough stock for an existing item" in {
      when(mockBasketService.add(any[String], any[String])) thenReturn Future(Left(InsufficientStockError))

      val req = FakeRequest(POST, "/shoppingbasket/update/id123")
        .withHeaders(BasketHeaderId -> basketId)

      val result = call(controller.addItemToBasket(basketId), req)
      assert(status(result) == BAD_REQUEST)
    }

    "inform the user that the item is not valid" in {
      when(mockBasketService.add(any[String], any[String])) thenReturn Future(Left(MissingItemError))

      val req = FakeRequest(POST, "/shoppingbasket/update/id123554")
        .withHeaders(BasketHeaderId -> basketId)

      val result = call(controller.addItemToBasket(basketId), req)
      assert(status(result) == NOT_FOUND)
    }

    "remove an existing item from the basket" in {
      when(mockBasketService.remove(any[String], any[String])) thenReturn Future(Right(BasketServiceSuccess))

      val req = FakeRequest(DELETE, "/shoppingbasket/update/id123")
        .withHeaders(BasketHeaderId -> basketId)

      val result = controller.deleteItemFromBasket(basketId)(req)
      assert(status(result) == OK)
    }

    "inform the user that there is no such item in the basket" in {
      when(mockBasketService.remove(any[String], any[String])) thenReturn Future(Left(MissingItemError))

      val req = FakeRequest(DELETE, "/shoppingbasket/update/id12345")
        .withHeaders(BasketHeaderId -> basketId)

      val result = controller.deleteItemFromBasket(basketId)(req)
      assert(status(result) == NOT_FOUND)
    }

  }
}
