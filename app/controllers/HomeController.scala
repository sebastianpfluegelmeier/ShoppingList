package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.typedmap.TypedKey
import play.api.i18n._
import javax.inject._
import play.api._
import play.api.mvc._
import models._
import slick.jdbc.H2Profile.api._
import scala.concurrent.duration.Duration
import concurrent.ExecutionContext.Implicits.global
import slick.jdbc.meta.MTable

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) with DatabaseSchema with play.api.i18n.I18nSupport {

  val db = Database.forConfig("h2")

  private val postUrl = routes.HomeController.createUser()

  def setup() = Action { implicit request: Request[AnyContent] => 

    scala.concurrent.Await.result(
      db.run(MTable.getTables).flatMap(tables => 
        db.run(allSchemas.createIfNotExists)
      )
      , Duration.Inf
    )
    scala.concurrent.Await.result(
      db.run(DBIO.seq(persons.delete))
      , Duration.Inf
    )
    scala.concurrent.Await.result(
      db.run(DBIO.seq(
        persons += Person(0, "Markus", "1234"),
        persons += Person(1, "Anton", "1234"),
        persons += Person(2, "Hannah", "1234"),
        persons += Person(3, "Sarah", "1234")
      ))
      , Duration.Inf
    )
    Ok("")

  }

  def index() = Action { implicit request: Request[AnyContent] =>


    val q = persons.map(_.name)
    val action = q.result
    val result: scala.concurrent.Future[Seq[String]] = db.run(action)
    val person = scala.concurrent.Await.result(result, Duration.Inf)

    Ok(views.html.index(person))
  }

  def users() = Action { implicit request: MessagesRequest[AnyContent] => 

    val q = persons.map(p => (p.id, p.name, p.password))
    val action = q.result
    val result: scala.concurrent.Future[Seq[(Long, String, String)]] = db.run(action)
    val personsList = scala.concurrent.Await.result(result, Duration.Inf).map(p => Person.apply(p._1, p._2, p._3))


    Ok(views.html.newPerson(personsList, userForm, postUrl))
  }

  def deleteUser() = Action { implicit request: MessagesRequest[AnyContent] => 

    val errorFunction = { formWithErrors: Form[Person] => 
      val q = persons.map(p => (p.id, p.name, p.password))
      val action = q.result
      val result: scala.concurrent.Future[Seq[(Long, String, String)]] = db.run(action)
      val personsList = scala.concurrent.Await.result(result, Duration.Inf).map(p => Person.apply(p._1, p._2, p._3))
      BadRequest(views.html.newPerson(personsList, userForm, postUrl))
    }

    val successFunction = { person: Person => 
      var idToDelete = person.id
      scala.concurrent.Await.result(db.run(persons.filter(p => p.id === idToDelete).delete), Duration.Inf)
      Redirect(routes.HomeController.index())
    }

    userForm.bindFromRequest.fold(errorFunction, successFunction)
  }

  def createUser() = Action { implicit request: MessagesRequest[AnyContent] => 

    val errorFunction = { formWithErrors: Form[Person] =>
      val q = persons.map(p => (p.id, p.name, p.password))
      val action = q.result
      val result: scala.concurrent.Future[Seq[(Long, String, String)]] = db.run(action)
      val personsList = scala.concurrent.Await.result(result, Duration.Inf).map(p => Person.apply(p._1, p._2, p._3))
      BadRequest(views.html.newPerson(personsList, userForm, postUrl))
    }

    val successFunction = { person: Person =>
      scala.concurrent.Await.result(db.run(persons += person), Duration.Inf)
      Redirect(routes.HomeController.users())//.flashing("info" -> "Person added!")
    }

    val formValidationResult = userForm.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }

  val userForm = Form(
    mapping(
      "id" -> longNumber,
      "name" -> text,
      "password"  -> text
    )(Person.apply)(Person.unapply)
  )
}
