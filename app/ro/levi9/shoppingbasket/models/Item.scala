package ro.levi9.shoppingbasket.models

import play.api.libs.json.Json

case class Item(
  id: String,
  name: String,
  vendor: String,
  category: String,
  description: String,
  price: Float
)

case object Item {
  implicit val writes = Json.writes[Item]
}