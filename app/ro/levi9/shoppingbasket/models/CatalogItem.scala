package ro.levi9.shoppingbasket.models

import play.api.libs.json.Json

case class CatalogItem(
  item: Item,
  stock: Int
)

case object CatalogItem {
  implicit val jsonFWrites = Json.writes[CatalogItem]
}
