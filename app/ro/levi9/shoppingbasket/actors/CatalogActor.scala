package ro.levi9.shoppingbasket.actors

import akka.actor.Actor
import ro.levi9.shoppingbasket.actors.messages.CatalogMessage._
import ro.levi9.shoppingbasket.models.{CatalogItem, Item}


class CatalogActor extends Actor {

  private val catalog = List(
    CatalogItem(Item("id123", "OnePlus3", "OnePlus", "phones", "the latest OnePlus smart phone", 1600), 2),
    CatalogItem(Item("id124", "OnePlus5", "OnePlus", "phones", "the latest OnePlus smart phone", 2500), 10),
    CatalogItem(Item("id125", "Iphone8", "Apple", "phones", "the latest OnePlus smart phone", 9998), 3),
    CatalogItem(Item("id126", "IphoneX", "Apple", "phones", "the latest OnePlus smart phone", 9999), 4)
  )

  def receive: Receive = update(catalog.map(i => i.item.id -> i).toMap)

  private def update(catalog: Map[String, CatalogItem]): Receive = {
    case DecreaseStockFor(itemId) =>
      catalog.get(itemId) match {
        case Some(item) =>
          if (item.stock > 0) {
            val updatedCatalog = catalog + (itemId -> item.copy(stock = item.stock - 1))
            context become update(updatedCatalog)
            sender ! ItemStockUpdated
          }
          else
            sender ! ItemOutOfStock

        case None => sender ! ItemNotFound
      }

    case IncreaseStockFor(itemId) =>
      catalog.get(itemId) match {
        case Some(item) =>
          val updatedCatalog = catalog + (itemId -> item.copy(stock = item.stock + 1))
          context become update(updatedCatalog)
          sender ! ItemStockUpdated

        case None => sender ! ItemNotFound
      }

    case GetItems(ids) =>
      sender ! catalog.filter(i => ids.contains(i._1))

    case ListAllItems =>
      sender ! catalog.values.toList
  }

}
