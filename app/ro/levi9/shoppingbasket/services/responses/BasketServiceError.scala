package ro.levi9.shoppingbasket.services.responses

trait BasketServiceError {
  val message: String
}

object BasketServiceError {
  case object UnexpectedMessageError extends BasketServiceError { override val message: String = "There was an internal error!" }
  case object InsufficientStockError extends BasketServiceError { override val message: String = "Requested item is out of stock!" }
  case object MissingItemError extends BasketServiceError { override val message: String = "Requested item was not found!" }
  case object MissingBasketError extends BasketServiceError { override val message: String = "Requested basket was not found!" }
  case object NoBasketIdProvidedError extends BasketServiceError { override val message: String = "Basket id was not provided. Check the headers!" }
}

