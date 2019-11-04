package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models._
import slick.jdbc.H2Profile.api._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with DatabaseSchema {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    DBIO.seq(
      persons += Person(0, "Markus", "1234")
    )
    def personsQuery: scala.concurrent.Future[Seq[Persons]] = {
      val query = for {
        p <- persons
      } yield (p.name, p.password)
      run(query)
    }
    def personsResult: (String, String) = scala.concurrent.Await.result(personsQuery.synchronized(), Duration.Inf)

    Ok(personsResult._1 + " " + personsResult._2)
  }
}
