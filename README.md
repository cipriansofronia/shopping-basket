[![Build Status](https://travis-ci.org/cipriansofronia/shopping-basket.svg?branch=master)](https://travis-ci.org/cipriansofronia/shopping-basket)

## shopping-basket

Small REST shopping-basket API built in scala with [Play Framework](https://www.playframework.com/) and [Akka](http://akka.io/)

## API


### Get the entire catalog
`GET /catalog` 
- response:
    - body:
        ```
        [
            {
                "item": {
                    "id": "id123",
                    "name": "OnePlus3",
                    "vendor": "OnePlus",
                    "category": "phones",
                    "description": "the latest OnePlus smart phone",
                    "price": 1600
                },
                "stock": 2
            },
            ...
        ]
        ```
    - status: OK


### Get the items for a specific basket
`GET /basket` 
- required headers: `X-Basket-Id` → `some-random-basket-id`
    - if `X-Basket-Id` header was not provided, than a new basket will be created and you'll get the new id in response as a header
- response:
    - body:
        ```
            {
                "products": [
                    {
                        "id": "id123",
                        "name": "OnePlus3",
                        "vendor": "OnePlus",
                        "description": "the latest OnePlus smart phone",
                        "price": 1600,
                        "amount": 2
                    }
                ]
            }
            ```
    - status: OK.withHeader(`X-Basket-Id` → `some-random-basket-id`)


### Add an item to basket
`POST /basket/items` 
- required headers: `X-Basket-Id` → `some-random-basket-id`
- required body:
    ```
    {
        "itemId": "id123",
        "amount": 1
    }
    ```
- response:
    - status: OK.withHeader(`X-Basket-Id` → `some-random-basket-id`)


### Delete an item from basket
`DELETE /basket/items/:itemId`
- required headers: `X-Basket-Id` → `some-random-basket-id`
- response:
    - status: OK.withHeader(`X-Basket-Id` → `some-random-basket-id`)

