package ro.levi9.shoppingbasket

import ro.levi9.shoppingbasket.dto.{ItemDTO, UpdateItemDTO}
import ro.levi9.shoppingbasket.models.{Basket, BasketItem, CatalogItem, Item}

trait TestValues {

  val BasketHeaderValue = "test-basket-id"

  val phone = Item("id123", "OnePlus3", "OnePlus", "phones", "the latest OnePlus smart phone", 1600)
  val catalogPhone = CatalogItem(phone, 2)
  val catalogItemDto = UpdateItemDTO(phone.id, 2)
  val catalogMap = Map(phone.id -> catalogPhone)
  val catalogList = List(catalogPhone)

  val basketItemDto = ItemDTO(catalogPhone, 2)
  val basketPhoneItem = BasketItem(phone.id, 2)
  val defaultBasket = Basket(Set(basketPhoneItem))

}
