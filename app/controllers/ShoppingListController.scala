package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}


@Singleton
class ShoppingListController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao
  
  def newShoppingList(householdId: String) = Action { implicit request: Request[AnyContent] => 
    Ok("")
  }

}