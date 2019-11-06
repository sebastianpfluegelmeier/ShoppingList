package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import models._
import javax.inject._
import play.api.libs.typedmap.{TypedKey, TypedMap}


@Singleton
class HouseholdsController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {

  def dao = new Dao

  def index() = Action { implicit request: Request[AnyContent] =>
    val pid = request.session.get("userId").map(_.toLong)
    pid match {
        case Some(p: Long) => {
            val households = dao.getHouseholds(p)
            Ok(views.html.households(request.session.data.get("userName"), households))
        }
        case None => Redirect("login")
    }
  }

  def household(id: String) = Action { implicit request: Request[AnyContent] => 


    val household = dao.getHousehold(id.toLong)
    val people = dao.getPeopleFromHousehold(id.toLong)

    Ok(views.html.household(request.session.data.get("userName"), household, people))
  }

  def addPerson() = Action { implicit request: Request[AnyContent] => 
    val name = request.body.asFormUrlEncoded.map(_.get("name").map(_(0))).flatten
    val householdId = request.body.asFormUrlEncoded.map(_.get("householdId").map(_(0))).flatten.map(id => id.toLong)
    for {
      h <- householdId
      n <- name
    } yield (dao.addPersonToHousehold(h, n))
    householdId match {
      case Some(h: Long) => Redirect("household/" + householdId.get)
      case other => Redirect("households")
    }
  }

  def householdPost = Action { implicit request: Request[AnyContent] => 

    val pid = request.session.get("userId").map(_.toLong)
    val name = request.body.asFormUrlEncoded.map(m => m.get("name").map(m => m(0))).flatten

    (pid, name) match {
        case (Some(p: Long), Some(n: String)) => {

            dao.newHousehold(Household(None, n), p)
            val households = dao.getHouseholds(p)
            Ok(views.html.households(request.session.data.get("userName"), households))
        }
        case other => Redirect("login")
    }
  }
}