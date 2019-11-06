package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}


@Singleton
class LoginController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao

  def loginForm() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.login())
  }

  def loginAction() = Action { implicit request: Request[AnyContent] =>
    val name = request.body.asFormUrlEncoded.map(m => m.get("name").map(m => m(0))).flatten
    val password = request.body.asFormUrlEncoded.map(m => m.get("password").map(m => m(0))).flatten

    (name, password) match {
      case (Some(name: String), Some(password: String)) => {
        if (dao.personExists(name, password)) {
            val id = dao.getPerson(name, password)
            val loginCredsSession = request.session + ("userId", id.toString()) + ("userName", name.toString())
            Ok("welcome").withSession(loginCredsSession)
        } else {
            Ok("name/password unknown")
        }
      }
      case other => Ok("no valid request")
    }
  }
}