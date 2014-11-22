package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(
      views.html.index("Tozny Title", "<h1>This is Tozny</h1>")
    )
  }

}
