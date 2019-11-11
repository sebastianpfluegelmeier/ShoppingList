package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n._
import javax.inject._
import play.api._
import play.api.mvc._
import models._
import slick.jdbc.H2Profile.api._
import scala.concurrent.duration.Duration
// import concurrent.ExecutionContext.Implicits.global
import slick.jdbc.meta.MTable

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new models.Dao

  def setup() = Action {
    dao.setup()
    Ok("setup done")
  }

  def index() = Action { implicit request: Request[AnyContent] =>
    var username = request.session.get("userName")
    Ok(views.html.index(username))
  }


}
