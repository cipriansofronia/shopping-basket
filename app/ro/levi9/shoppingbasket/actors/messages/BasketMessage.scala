package ro.levi9.shoppingbasket.actors.messages

import ro.levi9.shoppingbasket.dto.UpdateItemDTO
import ro.levi9.shoppingbasket.models.{Basket, BasketItem}

trait BasketInboundMessage
trait BasketOutboundMessage

object BasketMessage {
  case object CreateBasket extends BasketInboundMessage
  case class AddToBasket(basketId: String, item: UpdateItemDTO) extends BasketInboundMessage
  case class DeleteFromBasket(basketId: String, itemId: String) extends BasketInboundMessage
  case class GetBasket(basketId: String) extends BasketInboundMessage

  case class BasketCreated(basketId: String) extends BasketOutboundMessage
  case class BasketFound(basket: Basket) extends BasketOutboundMessage
  case object BasketItemAdded extends BasketOutboundMessage
  case class BasketItemRemoved(basketItem: BasketItem) extends BasketOutboundMessage
  case object BasketItemNotFound extends BasketOutboundMessage
  case object BasketNotFound extends BasketOutboundMessage
}
