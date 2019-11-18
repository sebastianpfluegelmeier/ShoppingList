package models

import scala.concurrent.duration.Duration
import slick.jdbc.SQLiteProfile.api._
import slick.jdbc.meta.MTable
import concurrent.ExecutionContext
import concurrent.ExecutionContext.Implicits.global
 

class Dao extends DatabaseSchema {


  val db = Database.forConfig("sqlite")

  def runDbOperation[T](operation: DBIOAction[T, slick.dbio.NoStream, Nothing]): T = {
    scala.concurrent.Await.result(
      db.run(operation)
      , Duration.Inf
    )
  }

  def newPerson(person: Person) = {
    runDbOperation(persons += person)
  }

  def personExists(name: String, password: String): Boolean = {
    runDbOperation(
      persons
        .filter(p => p.name === name && p.password === password)
        .result
    ).length > 0
  }

  def getPerson(name: String, password: String) = {
    runDbOperation(
      persons
        .filter(p => p.name === name && p.password === password)
        .result 
    )(0)
  }

  def newHousehold(household: Household, personId: Long) = {
    val householdId = runDbOperation(
      (households returning households.map(_.id)) += household
    )
    runDbOperation(personsHouseholds += PersonHousehold(personId, householdId))
  }

  def addPersonToHousehold(householdId: Long, personName: String) = {
    
    val personAlreadyInHousehold = runDbOperation(
      (for {
        ph <- personsHouseholds if ph.householdId === householdId
        p <- persons if p.id === ph.personId && p.name === personName
      } yield ph).result
    ).length > 0

    if (!personAlreadyInHousehold) {
      val personId: Option[Long] = runDbOperation(
        persons.filter(_.name === personName).result
      )(0).id

      runDbOperation(
        personsHouseholds += PersonHousehold(personId.get, householdId)
      )
    }
  }

  def removePersonFromHousehold(householdId: Long, personId: Long) = {
    runDbOperation(
      personsHouseholds
        .filter(ph => ph.personId === personId && ph.householdId === householdId)
        .delete
    )
  }

  def getPeopleFromHousehold(householdId: Long): Seq[Person] = {
    runDbOperation(
      ( for {
          ph <- personsHouseholds if ph.householdId === householdId
          p <- persons if p.id === ph.personId
        } yield p
      )
      .result
    )
  }

  def getShoppinglistsFromHousehold(householdId: Long): Seq[ShoppingList] = {
    runDbOperation(
      shoppingLists
        .filter(_.householdId === householdId)
        .result
    )
  }

  def newShoppingList(householdId: Long): Long = {
    scala.concurrent.Await.result(
        db.run((shoppingLists returning shoppingLists.map(_.id)) += ShoppingList(None, "new shoppinglist", householdId, false))
        , Duration.Inf
    )
  }

  def getShoppingList(shoppingListId: Long): (ShoppingList, Seq[ShoppingListItem]) = {

    val shoppingListItemsList = runDbOperation(
      shoppingListItems.filter(sli => sli.shoppingListId === shoppingListId).result
    )

    val shoppingList = runDbOperation(
      shoppingLists.filter(sl => sl.id === shoppingListId).result
    )(0)

    (shoppingList, shoppingListItemsList)
  }

  def upsertShoppingList(shoppingList: ShoppingList, items: List[ShoppingListItem]) = {
    runDbOperation(
      shoppingLists.insertOrUpdate(shoppingList)
    )

    items
      .map(item => 
        runDbOperation(shoppingListItems.insertOrUpdate(item))
      )
  }

  def removeHousehold(id: Long) = {

      runDbOperation(
        households.filter(h => h.id === id).delete
      )

      runDbOperation(
        personsHouseholds.filter(ph => ph.householdId === id).delete
      )
  }

  def getHouseholds(personId: Long): Seq[Household] = {
    runDbOperation(
      ( for {
          ph <- personsHouseholds if ph.personId === personId
          h <- households if h.id === ph.householdId
        } yield h
      )
      .result
    )
  }

  def getHousehold(id: Long): Household = {
    runDbOperation(
      households.filter(h => h.id === id).result
    )(0)
  }

  def disableShoppingList(id: Long) = {
    runDbOperation(
      ( for {
          s <- shoppingLists if s.id === id
        } yield (s.disabled)
      ).update(true)
    )
  }

  def getAllShoppingLists(personId: Long): Seq[ShoppingList] = {
    runDbOperation(
      ( for {
          ph <- personsHouseholds if ph.personId === personId
          h <- households if h.id === ph.householdId
          s <- shoppingLists if s.householdId === h.id
        } yield s
      ).result
    )
  }

  def getPurchases(personId: Long): Seq[Purchase] = {
    runDbOperation(
      purchases
        .filter(p => p.personId === personId)
        .result
    )
  }

  def getPurchase(id: Long): Purchase = {
    runDbOperation(
      purchases
        .filter(p => p.id === id)
        .result
    )(0)
  }

  def setPurchase(id: Long, purchase: Purchase) = {
    runDbOperation(
      purchases.insertOrUpdate(purchase)
    )
  }

  def newPurchase(userId: Long): Long = {
    runDbOperation(
      (purchases returning purchases.map(_.id)) += Purchase(None, "new purchase", userId, 0, false)
    )
  }

  def disablePurchase(id: Long) = {
    runDbOperation(
      ( for 
        { p <- purchases if p.id === id}
        yield (p.disabled)
      ).update(true)
    )

  }

  def setup(): Unit = { 

    runDbOperation(
      MTable
        .getTables
        .flatMap(
          tables => allSchemas.createIfNotExists
        )
    )

    runDbOperation(
      DBIO.seq(
        persons.delete,
        households.delete,
        shoppingLists.delete,
        shoppingListItems.delete,
        purchases.delete
      )
    )
  }
}