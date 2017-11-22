package ro.levi9.shoppingbasket.actors.messages


trait CatalogInboundMessage
trait CatalogOutboundMessage

object CatalogMessage {
  case object ListAllItems extends CatalogInboundMessage
  case class GetItems(ids: Set[String]) extends CatalogInboundMessage
  case class IncreaseStockFor(itemId: String) extends CatalogInboundMessage
  case class DecreaseStockFor(itemId: String) extends CatalogInboundMessage

  case object ItemStockUpdated extends CatalogOutboundMessage
  case object ItemNotFound extends CatalogOutboundMessage
  case object ItemOutOfStock extends CatalogOutboundMessage
}




