package ro.levi9.shoppingbasket.dto

import play.api.libs.json.JsonValidationError
import play.api.libs.json.{JsPath, Reads}
import play.api.libs.functional.syntax._

case class UpdateItemDTO(itemId: String, amount: Int)

case object UpdateItemDTO {

  val onlyPositiveValues: Reads[Int] =
    Reads.IntReads.filter(JsonValidationError("The amount requires values above 0!"))(value => value > 0)

  implicit val jsonReads: Reads[UpdateItemDTO] =
    ((JsPath \ "itemId").read[String] and
    (JsPath \ "amount").read[Int](onlyPositiveValues))(UpdateItemDTO.apply _)

}
