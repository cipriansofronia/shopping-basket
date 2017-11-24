package ro.levi9.shoppingbasket.controllers

import javax.inject._

import play.api.Configuration
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import ro.levi9.shoppingbasket.dto.{ErrorDTO, UpdateItemDTO}
import ro.levi9.shoppingbasket.services.{BasketService, CatalogService}
import ro.levi9.shoppingbasket.services.responses.BasketServiceError._
import ro.levi9.shoppingbasket.services.responses.BasketServiceResponse.{BasketCreatedSuccess, BasketDTOSuccess, BasketServiceSuccess}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MainController @Inject()(cc: ControllerComponents,
                               basketService: BasketService,
                               catalogService: CatalogService,
                               config: Configuration)
                              (implicit exec: ExecutionContext) extends AbstractController(cc) {

  private val BasketIdHeader = config.get[String]("play.filters.headers.x-basket-id")


  def listCatalog = Action.async { _ =>
    catalogService.listCatalog.map {
      catalog => Ok(Json.toJson(catalog))
    }
  }

  def listBasket = Action.async { request =>
    request.headers.get(BasketIdHeader) match {
      case Some(basketId) if basketId.nonEmpty =>
        basketService.listBasket(basketId).map {
          case Right(BasketDTOSuccess(basket)) => Ok(Json.toJson(basket)).withHeaders(BasketIdHeader -> basketId)
          case Left(MissingBasketError) => NotFound(Json.toJson(ErrorDTO(MissingBasketError.message)))
          case _ => InternalServerError(Json.toJson(ErrorDTO(UnexpectedMessageError.message)))
        }

      case None =>
        Future.successful(Forbidden(Json.toJson(ErrorDTO(NoBasketIdProvidedError.message))))
    }
  }

  def addItemToBasket = Action.async(parse.json) { request =>
    request.body.validate[UpdateItemDTO].fold(
      errors =>
        Future.successful(BadRequest(JsError.toJson(errors))),
      item =>
        request.headers.get(BasketIdHeader) match {
          case Some(basketId) if basketId.nonEmpty => addToBasket(basketId, item)
          case None =>
            basketService.createBasket.flatMap {
              case Right(BasketCreatedSuccess(basketId)) => addToBasket(basketId, item)
              case _ => Future.successful(InternalServerError(Json.toJson(ErrorDTO(UnexpectedMessageError.message))))
            }
        }
    )
  }

  private def addToBasket(basketId: String, item: UpdateItemDTO): Future[Result] =
    basketService.add(basketId, item).map {
      case Right(BasketServiceSuccess) => Created.withHeaders(BasketIdHeader -> basketId)
      case Left(MissingItemError) => NotFound(Json.toJson(ErrorDTO(MissingItemError.message)))
      case Left(MissingBasketError) => NotFound(Json.toJson(ErrorDTO(MissingBasketError.message)))
      case Left(InsufficientStockError) => BadRequest(Json.toJson(ErrorDTO(InsufficientStockError.message)))
      case _ => InternalServerError(Json.toJson(ErrorDTO(UnexpectedMessageError.message)))
    }

  def deleteItemFromBasket(itemId: String) = Action.async { request =>
    request.headers.get(BasketIdHeader) match {
      case Some(basketId) if basketId.nonEmpty =>
        basketService.remove(basketId, itemId).map {
          case Right(BasketServiceSuccess) => Ok.withHeaders(BasketIdHeader -> basketId)
          case Left(MissingItemError) => NotFound(Json.toJson(ErrorDTO(MissingItemError.message)))
          case Left(MissingBasketError) => NotFound(Json.toJson(ErrorDTO(MissingBasketError.message)))
          case _ => InternalServerError(Json.toJson(ErrorDTO(UnexpectedMessageError.message)))
        }

      case None =>
        Future.successful(Forbidden(Json.toJson(ErrorDTO(NoBasketIdProvidedError.message))))
    }
  }

}
