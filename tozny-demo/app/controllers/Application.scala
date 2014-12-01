package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(
      views.html.index(
        "Tozny Title", 
        "This is a Tozny example site", 
        Play.current.configuration.getString("tozny.realmKeyId").get,
        Play.current.configuration.getString("tozny.apiUrl").getOrElse("https://api.tozny.com")
      )
    )
  }

}
