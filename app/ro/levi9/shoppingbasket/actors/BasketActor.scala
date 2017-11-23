package ro.levi9.shoppingbasket.actors

import java.util.UUID

import akka.actor.Actor
import ro.levi9.shoppingbasket.actors.messages.BasketMessage._
import ro.levi9.shoppingbasket.models.{Basket, BasketItem}


class BasketActor extends Actor {

  def receive: Receive = update(Map.empty[String, Basket])

  private def update(basket: Map[String, Basket]): Receive = {

    case CreateBasket =>
      val newBasketId = UUID.randomUUID().toString
      val newBasket = basket + (newBasketId -> Basket(Set.empty[BasketItem]))
      context become update(newBasket)
      sender ! BasketCreated(newBasketId)


    case GetBasket(basketId) =>
      basket.get(basketId) match {
        case Some(foundBasket) => sender ! BasketFound(foundBasket)
        case None => sender ! BasketNotFound
      }


    case AddToBasket(basketId, itemDTO) =>
      basket.get(basketId) match {
        case Some(foundBasket) =>
          val updatedBasket = basket + (basketId -> Basket(foundBasket.addOrUpdateItem(itemDTO.itemId, itemDTO.amount)))
          context become update(updatedBasket)
          sender ! BasketItemAdded

        case None =>
          sender ! BasketNotFound
      }


    case DeleteFromBasket(basketId, itemId) =>
      basket.get(basketId) match {
        case Some(foundBasket) =>
          foundBasket.getBasketItem(itemId) match {
            case Some(basketItem) =>
              val updatedBasket = basket + (basketId -> Basket(foundBasket.deleteItem(basketItem)))
              context become update(updatedBasket)
              sender ! BasketItemRemoved(basketItem)

            case None =>
              sender ! BasketItemNotFound
          }

        case None =>
          sender ! BasketNotFound

      }

  }

}



