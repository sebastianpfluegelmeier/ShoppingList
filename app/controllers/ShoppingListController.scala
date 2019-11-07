package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.libs.json._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}


@Singleton
class ShoppingListController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao
  
  def newShoppingList(householdId: String) = Action { implicit request: Request[AnyContent] => 
    var username = request.session.get("userName")
    var shoppingListId = dao.newShoppingList(householdId.toLong)
    Ok(views.html.newShoppingList(username, shoppingListId))
  }

  def getShoppingList(shoppingListId: String) = Action { implicit request: Request[AnyContent] => 
    val (shoppingList, shoppingListItems) = dao.getShoppingList(shoppingListId.toLong)
    val json: JsValue = Json.obj(
      "name" -> shoppingList.name,
      "list" -> 
        Json.arr(
          shoppingListItems.map(
            sli => Json.obj(
              "id" -> sli.id.get,
              "name" -> sli.name
            )
        ))(0)
    )
    Ok(json)
  }

  def postShoppingList() = Action { implicit request: Request[AnyContent] => 
    request.body.asJson.map{ json => 
      val name = (json \ "name").get
      val list = (json \ "list")//map(item => ((item \ "id"), (item \ "name")))

    }
    // dao.setShoppingList()
    Ok("")
  }
}