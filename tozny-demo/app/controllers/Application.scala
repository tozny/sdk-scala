package controllers

import org.apache.commons.codec.binary.Base64
import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

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
