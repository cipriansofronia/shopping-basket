package ro.levi9.shoppingbasket.dto

import play.api.libs.json._


case class ErrorDTO(description: String)

object ErrorDTO {
  implicit val writes: Writes[ErrorDTO] = Json.writes[ErrorDTO]
}
