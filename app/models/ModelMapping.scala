package models

import slick.jdbc.H2Profile.api._

trait DatabaseSchema {

    class Persons(tag: Tag) extends Table[Person](tag, "PERSON") {
        def id = column[Long]("ID", O.PrimaryKey)
        def name = column[String]("NAME")
        def password = column[String]("PASSWORD")

        def * = (id, name, password) <> (Person.tupled, Person.unapply)
    }

    val persons = TableQuery[Persons]

    class Households(tag: Tag) extends Table[Household](tag, "HOUSEHOLD") {
        def id = column[Long]("ID", O.PrimaryKey)
        def name = column[String]("NAME")

        def * = (id, name) <> (Household.tupled, Household.unapply)
    }

    val households = TableQuery[Households]

    class PersonsHouseholds(tag: Tag) extends Table[PersonHousehold](tag, "PERSON_HOUSEHOLD") {
        def id = column[Long]("ID", O.PrimaryKey)


        def personId = column[Long]("PERSON_ID")
        def person = foreignKey("FK_PERSON_ID", personId, persons)(_.id)

        def householdId = column[Long]("HOUSEHOLD_ID")
        def household = foreignKey("FK_HOUSEHOLD_ID", householdId, households)(_.id)

        def * = (id, personId, householdId) <> (PersonHousehold.tupled, PersonHousehold.unapply)
    }

    val personsHouseholds = TableQuery[PersonsHouseholds]

    class ShoppingLists(tag: Tag) extends Table[ShoppingList](tag, "SHOPPING_LIST") {
        def id = column[Long]("ID", O.PrimaryKey)
        def name = column[String]("NAME")
        def householdId = column[Long]("HOUSEHOLD_ID")

        def household = foreignKey("FK_HOUSEHOLD_ID", householdId, households)(_.id)

        def * = (id, name, householdId) <> (ShoppingList.tupled, ShoppingList.unapply)
    }

    val shoppingLists = TableQuery[ShoppingLists]

    class ShoppingListItems(tag: Tag) extends Table[ShoppingListItem](tag, "SHOPPINGLIST_ITEM") {
        def id = column[Long]("ID", O.PrimaryKey)
        def shoppingListId = column[Long]("SHOPPING_LIST_ID")
        def name = column[String]("NAME")
        def purchaseId = column[Long]("HOUSEHOLD_ID")

        def shoppinglist = foreignKey("FK_SHOPPINGLIST_ID", shoppingListId, shoppingLists)(_.id)
        def purchase = foreignKey("FK_PURCHASE_ID", purchaseId, purchases)(_.id)

        def * = (id, shoppingListId, name, purchaseId.?) <> (ShoppingListItem.tupled, ShoppingListItem.unapply)
    }

    val shoppingListItems = TableQuery[ShoppingListItems]

    class Purchases(tag: Tag) extends Table[Purchase](tag, "PURCHASE") {
        def id = column[Long]("ID", O.PrimaryKey)
        def householdId = column[Long]("HOUSEHOLD_ID")
        def personId = column[Long]("PERSON_ID")
        def price = column[Int]("NAME")

        def household = foreignKey("FK_HOUSEHOLD_ID", householdId, households)(_.id)
        def person = foreignKey("FK_PERSON_ID", personId, persons)(_.id)

        def * = (id, householdId, personId, price) <> (Purchase.tupled, Purchase.unapply)
    }

    val purchases = TableQuery[Purchases]

    val allSchemas = persons.schema ++ households.schema ++ personsHouseholds.schema ++ shoppingLists.schema ++ shoppingListItems.schema ++ purchases.schema
}