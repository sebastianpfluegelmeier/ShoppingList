package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}


@Singleton
class CreateUserController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.createUser())
  }

  def createUser() = Action { implicit request: Request[AnyContent] =>
    val name = request.body.asFormUrlEncoded.map(m => m.get("name").map(m => m(0))).flatten
    val password = request.body.asFormUrlEncoded.map(m => m.get("password").map(m => m(0))).flatten

    (name, password) match {
      case (Some(name: String), Some(password: String)) => {
        dao.newPerson(Person(None, name, password))
        Redirect(routes.HomeController.index())
      }
      case other => Ok("no valid request")
    }
  }
}