package ro.levi9.shoppingbasket.controllers

import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import ro.levi9.shoppingbasket.TestValues
import ro.levi9.shoppingbasket.dto.{BasketDTO, ErrorDTO, UpdateItemDTO}
import ro.levi9.shoppingbasket.services.responses.BasketServiceError.{InsufficientStockError, MissingItemError}
import ro.levi9.shoppingbasket.services.responses.BasketServiceResponse.{BasketDTOSuccess, BasketServiceSuccess}
import ro.levi9.shoppingbasket.services.{BasketService, CatalogService}

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class MainControllerSpec extends PlaySpec
  with MockitoSugar
  with GuiceOneAppPerSuite
  with TestValues {

  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit lazy val materializer: Materializer = app.materializer

  private val config = Configuration(ConfigFactory.load("application.conf"))
  private val BasketHeaderKey = config.get[String]("play.filters.headers.x-basket-id")

  private val mockBasketService = mock[BasketService]
  private val mockCatalogService = mock[CatalogService]

  private val controller = new GuiceApplicationBuilder()
    .configure(config)
    .overrides(bind[BasketService].toInstance(mockBasketService))
    .overrides(bind[CatalogService].toInstance(mockCatalogService))
    .build()
    .injector
    .instanceOf[MainController]


  "A MainController" should {

    "return a list of items in the catalog" in {
      when(mockCatalogService.listCatalog) thenReturn Future(catalogList)

      val result = controller.listCatalog(FakeRequest(GET, "/catalog"))
      assert(status(result) ==  OK)
      assert(contentAsJson(result) ==  Json.toJson(catalogList))
    }

    "return a list of items in the basket" in {
      when(mockBasketService.listBasket(any[String])) thenReturn Future(Right(BasketDTOSuccess(BasketDTO(Set(basketItemDto)))))

      val result = controller.listBasket(FakeRequest(GET, "/basket").withHeaders(BasketHeaderKey -> BasketHeaderValue))
      assert(status(result) ==  OK)
      assert(contentAsJson(result) ==  Json.toJson(BasketDTO(Set(basketItemDto))))
    }

    "successfully add an existing item if the stock is sufficient" in {
      when(mockBasketService.add(any[String], any[UpdateItemDTO])) thenReturn Future(Right(BasketServiceSuccess))

      val req = FakeRequest(POST, "/basket/items")
        .withBody("""{"itemId":"id123","amount":1}""")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withHeaders(BasketHeaderKey -> BasketHeaderValue)

      val result = call(controller.addItemToBasket, req)
      assert(status(result) == CREATED)
    }


    "inform the user that there is not enough stock for an existing item" in {
      when(mockBasketService.add(any[String], any[UpdateItemDTO])) thenReturn Future(Left(InsufficientStockError))

      val req = FakeRequest(POST, "/basket/items")
        .withBody("""{"itemId":"id123","amount":2}""")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withHeaders(BasketHeaderKey -> BasketHeaderValue)

      val result = call(controller.addItemToBasket, req)
      assert(status(result) == BAD_REQUEST)
      assert(contentAsJson(result) ==  Json.toJson(ErrorDTO(InsufficientStockError.message)))
    }

    "inform the user that the item is not found" in {
      when(mockBasketService.add(any[String], any[UpdateItemDTO])) thenReturn Future(Left(MissingItemError))

      val req = FakeRequest(POST, "/basket/items")
        .withBody("""{"itemId":"id123","amount":1}""")
        .withHeaders(CONTENT_TYPE -> "application/json")
        .withHeaders(BasketHeaderKey -> BasketHeaderValue)

      val result = call(controller.addItemToBasket, req)
      assert(status(result) == NOT_FOUND)
      assert(contentAsJson(result) ==  Json.toJson(ErrorDTO(MissingItemError.message)))
    }

    "remove an existing item from the basket" in {
      when(mockBasketService.remove(any[String], any[String])) thenReturn Future(Right(BasketServiceSuccess))

      val req = FakeRequest(DELETE, "/basket/items/id123")
        .withHeaders(BasketHeaderKey -> BasketHeaderValue)

      val result = controller.deleteItemFromBasket(BasketHeaderValue)(req)
      assert(status(result) == OK)
    }

    "inform the user that there is no such item in the basket" in {
      when(mockBasketService.remove(any[String], any[String])) thenReturn Future(Left(MissingItemError))

      val req = FakeRequest(DELETE, "/basket/items/id12345")
        .withHeaders(BasketHeaderKey -> BasketHeaderValue)

      val result = controller.deleteItemFromBasket(BasketHeaderValue)(req)
      assert(status(result) == NOT_FOUND)
      assert(contentAsJson(result) ==  Json.toJson(ErrorDTO(MissingItemError.message)))
    }

  }
}
