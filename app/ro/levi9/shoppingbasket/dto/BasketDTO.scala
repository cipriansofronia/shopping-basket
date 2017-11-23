package ro.levi9.shoppingbasket.dto

import play.api.libs.json.{Writes, Json}
import ro.levi9.shoppingbasket.models.{Basket, CatalogItem}


case class BasketDTO(products: Set[ItemDTO])

object BasketDTO {
  implicit val jsonFormat: Writes[BasketDTO] = Json.writes[BasketDTO]

  def apply(basket: Basket, products: Map[String, CatalogItem]): BasketDTO =
    new BasketDTO(basket.items.map(item => ItemDTO(products(item.itemId), item.amount)))
}


case class ItemDTO(id: String, name: String, vendor: String, description: String, price: Double, amount: Int)

case object ItemDTO {
  implicit val jsonFormat: Writes[ItemDTO] = Json.writes[ItemDTO]

  def apply(cItem: CatalogItem, amount: Int): ItemDTO =
    new ItemDTO(cItem.item.id, cItem.item.name, cItem.item.vendor, cItem.item.description, cItem.item.price, amount)
}
