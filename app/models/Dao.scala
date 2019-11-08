package models

import scala.concurrent.duration.Duration
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta.MTable
import concurrent.ExecutionContext.Implicits.global

class Dao extends DatabaseSchema {

  val db = Database.forConfig("h2")

  def runDbOperation(operation: DBIOAction[Any, slick.dbio.NoStream, Nothing]) = {
    scala.concurrent.Await.result(
        db.run(operation)
        , Duration.Inf
    )
    ()
  }

  def newPerson(person: Person) = {
      runDbOperation(persons += person)
  }

  def personExists(name: String, password: String) = {
    scala.concurrent.Await.result(
      db.run(persons.filter(p => p.name === name && p.password === password).result), 
      Duration.Inf 
    ).length > 0
  }

  def getPerson(name: String, password: String) = {
    scala.concurrent.Await.result(
      db.run(persons.filter(p => p.name === name && p.password === password).result), 
      Duration.Inf 
    )(0)
  }

  def removePerson(id: Long) = {
      runDbOperation(persons.filter(p => p.id === id).delete)
  }

  def getPerson(id: Long): Person = {
    scala.concurrent.Await.result(
        db.run(persons.filter(p => p.id === id).result)
        , Duration.Inf
    )(0)
  }

  def getPerson(name: String): Person = {
    scala.concurrent.Await.result(
        db.run(persons.filter(p => p.name === name).result)
        , Duration.Inf
    )(0)
  }

  def getAllPersons(): Seq[Person] = {
    scala.concurrent.Await.result(
        db.run(persons.result)
        , Duration.Inf
    )
  }

  def newHousehold(household: Household, personId: Long) = {
    val householdId = scala.concurrent.Await.result(
      db.run((households returning households.map(_.id)) += household)
      , Duration.Inf
    )
    runDbOperation(personsHouseholds += PersonHousehold(personId, householdId))
  }

  def addPersonToHousehold(householdId: Long, personName: String) = {
    val personAlreadyInHousehold = scala.concurrent.Await.result(
      db.run(
          (for {
            ph <- personsHouseholds if ph.householdId === householdId
            p <- persons if p.id === ph.personId && p.name === personName
          } yield ph).result)
      , Duration.Inf
    ).length > 0
    if (!personAlreadyInHousehold) {
      val personId: Option[Long] = scala.concurrent.Await.result(
        db.run(persons.filter(p => p.name === personName).result)
        , Duration.Inf
      )(0).id
      runDbOperation(personsHouseholds += PersonHousehold(personId.get, householdId))
    }
  }

  def removePersonFromHousehold(householdId: Long, personId: Long) = {
    runDbOperation(personsHouseholds.filter(ph => ph.personId === personId && ph.householdId === householdId).delete)
  }

  def getPeopleFromHousehold(householdId: Long): Seq[Person] = {
    scala.concurrent.Await.result(
        db.run(
          (for {
            ph <- personsHouseholds if ph.householdId === householdId
            p <- persons if p.id === ph.personId
          } yield p)
          .result
          )
        , Duration.Inf
    )
  }

  def getShoppinglistsFromHousehold(householdId: Long): Seq[ShoppingList] = {
    scala.concurrent.Await.result(
        db.run(shoppingLists.filter(s => s.householdId === householdId).result)
        , Duration.Inf
    )
  }

  def newShoppingList(householdId: Long): Long = {
    scala.concurrent.Await.result(
        db.run((shoppingLists returning shoppingLists.map(_.id)) += ShoppingList(None, "new shoppinglist", householdId))
        , Duration.Inf
    )
  }

  def getShoppingList(shoppingListId: Long): (ShoppingList, Seq[ShoppingListItem]) = {

    val shoppingListItemsList = scala.concurrent.Await.result(
        db.run(shoppingListItems.filter(sli => sli.shoppingListId === shoppingListId).result)
        , Duration.Inf
      )


    val shoppingList = scala.concurrent.Await.result(
        db.run(shoppingLists.filter(sl => sl.id === shoppingListId).result)
        , Duration.Inf
      )(0)

    
    (shoppingList, shoppingListItemsList)

  }

  def upsertShoppingList(shoppingList: ShoppingList, items: List[ShoppingListItem]) = {
    runDbOperation(shoppingLists += shoppingList)
    items.map(item => runDbOperation(shoppingListItems.insertOrUpdate(item)))
  }

  def removeHousehold(id: Long) = {
      runDbOperation(households.filter(h => h.id === id).delete)
      runDbOperation(personsHouseholds.filter(ph => ph.householdId === id).delete)
  }

  def getHouseholds(personId: Long): Seq[Household] = {
    scala.concurrent.Await.result(
        db.run(
          (for {
            ph <- personsHouseholds if ph.personId === personId
            h <- households if h.id === ph.householdId
          } yield h)
          .result
          )
        , Duration.Inf
    )
  }

  def getHousehold(id: Long): Household = {
    scala.concurrent.Await.result(
        db.run(households.filter(h => h.id === id).result)
        , Duration.Inf
    )(0)
  }

  /*
  def getAllHouseholdsForPerson(id: Long): Seq[Household] = {
    scala.concurrent.Await.result(
        db.run(households.join(personsHouseholds).filter(h => ).result)
        , Duration.Inf
    )
  }
  */


  def setup(): Unit = { 

    scala.concurrent.Await.result(
      db.run(MTable.getTables).flatMap(tables => 
        db.run(allSchemas.createIfNotExists)
      )
      , Duration.Inf
    )

    runDbOperation(DBIO.seq(persons.delete, households.delete, shoppingLists.delete, shoppingListItems.delete, purchases.delete))
  }
}