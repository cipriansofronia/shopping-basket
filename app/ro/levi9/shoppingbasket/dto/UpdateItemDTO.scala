package ro.levi9.shoppingbasket.dto

import play.api.libs.json.{Json, Reads}

case class UpdateItemDTO(itemId: String, amount: Int) {
  def abs: UpdateItemDTO = copy(amount = amount.abs)
}

case object UpdateItemDTO {
  implicit val jsonReads: Reads[UpdateItemDTO] = Json.reads[UpdateItemDTO]
}
