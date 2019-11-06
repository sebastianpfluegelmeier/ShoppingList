package models

case class Person(id: Option[Long], name: String, password: String)

case class Household(id: Option[Long], name: String)

case class PersonHousehold(personId: Long, householdId: Long)

case class ShoppingList(id: Option[Long], name: String, householdId: Long)

case class ShoppingListItem(id: Option[Long], shoppingListId: Long, name: String, purchaseId: Option[Long])

case class Purchase(id: Option[Long], personId: Long, price: Int)