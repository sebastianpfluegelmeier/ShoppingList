package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}
import play.api.libs.json._ 
import play.api.libs.json.Reads._ 
import play.api.libs.functional.syntax._ 


@Singleton
class ShoppingListController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao
  
  def newShoppingList(householdId: String) = Action { implicit request: Request[AnyContent] => 
    var username = request.session.get("userName")
    var shoppingListId = dao.newShoppingList(householdId.toLong)
    Ok(views.html.newShoppingList(username, shoppingListId))
  }

  def getShoppingList(shoppingListIdString: String) = Action { implicit request: Request[AnyContent] => 
    var username = request.session.get("userName")
    var shoppingListId = shoppingListIdString.toLong
    Ok(views.html.newShoppingList(username, shoppingListId))
  }

  def getShoppingListJson(shoppingListId: String) = Action { implicit request: Request[AnyContent] => 
    val (shoppingList, shoppingListItems) = dao.getShoppingList(shoppingListId.toLong)
    val json: JsValue = Json.obj(
      "name" -> shoppingList.name,
      "householdId" -> shoppingList.householdId,
      "id" -> shoppingList.id,
      "list" -> 
        Json.arr(
          shoppingListItems.map(
            sli => Json.obj(
              "id" -> sli.id.get,
              "name" -> sli.name,
              "purchaseId" -> sli.purchaseId
            )
        ))(0)
    )
    Ok(json)
  }


  case class ShoppingListJson(name: String, id: Long, householdId: Long, list: List[ShoppingListItemJson])
  case class ShoppingListItemJson(name: String, id: Long, purchaseId: Option[Long])

  implicit val readsShoppingListItem: Reads[ShoppingListItemJson] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "id").read[Long] and
      (JsPath \ "purchaseId").formatNullable[Long]
    )(ShoppingListItemJson.apply(_, _,_))

  implicit val readsShoppingList: Reads[ShoppingListJson] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "id").read[Int] and
      (JsPath \ "householdId").read[Int] and
      (JsPath \ "list").read[List[ShoppingListItemJson]]
    )(ShoppingListJson.apply(_,_,_,_))

  def postShoppingList(shoppingListId: String) = Action { implicit request: Request[AnyContent] => 
    val shoppingListJson = Json.parse(request.body.asJson.get.toString).as[ShoppingListJson]
    val items = shoppingListJson.list.map(item => ShoppingListItem(Some(item.id), shoppingListJson.id, item.name, item.purchaseId))
    val shoppingList = ShoppingList(Some(shoppingListJson.id), shoppingListJson.name, shoppingListJson.householdId)
    dao.upsertShoppingList(shoppingList, items)
    Ok("")
  }
}