package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}


@Singleton
class AllShoppingListsController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao

  def allShoppingLists() = Action { implicit request: Request[AnyContent] =>
    val username = request.session.data.get("userName")
    val userId = request.session.data.get("userId")
    userId match {
        case Some(uid: String) => {
            val shoppingLists = dao.getAllShoppingLists(uid.toLong)
            Ok(views.html.allShoppingLists(username, shoppingLists))
        }
        case other => Redirect("login")

    }
  }
}