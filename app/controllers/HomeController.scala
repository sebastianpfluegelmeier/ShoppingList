package controllers

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
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with DatabaseSchema {

  val db = Database.forConfig("h2")

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

    setup()

    val q = persons.map(_.name)
    val action = q.result
    val result: scala.concurrent.Future[Seq[String]] = db.run(action)
    val person = scala.concurrent.Await.result(result, Duration.Inf)

    println(person)
    Ok(views.html.index(person))
  }
}
