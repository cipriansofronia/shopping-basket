package ro.levi9.shoppingbasket.models

case class Basket(items: Set[BasketItem]) {

  def addOrUpdateItem(itemId: String, amount: Int): Set[BasketItem] =
    getBasketItem(itemId) match {
      case Some(basketItem) =>
        deleteItem(basketItem) ++ {
          val newAmount = basketItem.amount + amount
          if (newAmount > 0) Set(basketItem.copy(amount = newAmount)) else Nil
        }

      case None =>
        items ++ Set(BasketItem(itemId, amount))
    }

  def deleteItem(basketItem: BasketItem): Set[BasketItem] =
    items.filterNot(_.itemId == basketItem.itemId)

  def getBasketItem(itemId: String): Option[BasketItem] = items.find(_.itemId == itemId)

}