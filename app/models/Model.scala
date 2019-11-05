package models

case class Person(id: Long, name: String, password: String)

case class Household(id: Long, name: String)

case class PersonHousehold(id: Long, personId: Long, householdId: Long)

case class ShoppingList(id: Long, name: String, householdId: Long)

case class ShoppingListItem(id: Long, shoppingListId: Long, name: String, purchaseId: Option[Long])

case class Purchase(id: Long, householdId: Long, personId: Long, price: Int)