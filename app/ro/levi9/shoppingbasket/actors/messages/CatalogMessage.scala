package ro.levi9.shoppingbasket.actors.messages

import ro.levi9.shoppingbasket.dto.UpdateItemDTO
import ro.levi9.shoppingbasket.models.BasketItem


trait CatalogInboundMessage
trait CatalogOutboundMessage

object CatalogMessage {
  case object ListAllItems extends CatalogInboundMessage
  case class GetItems(ids: Set[String]) extends CatalogInboundMessage
  case class IncreaseStockFor(basketItem: BasketItem) extends CatalogInboundMessage
  case class DecreaseStockFor(item: UpdateItemDTO) extends CatalogInboundMessage

  case object ItemStockUpdated extends CatalogOutboundMessage
  case object ItemNotFound extends CatalogOutboundMessage
  case object ItemOutOfStock extends CatalogOutboundMessage
}




