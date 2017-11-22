package ro.levi9.shoppingbasket.services.responses

import ro.levi9.shoppingbasket.dto.BasketDTO

trait BasketServiceResponse

object BasketServiceResponse {
  case class BasketCreatedSuccess(basketId: String) extends BasketServiceResponse
  case object BasketServiceSuccess extends BasketServiceResponse
  case class BasketDTOSuccess(basket: BasketDTO) extends BasketServiceResponse
}



