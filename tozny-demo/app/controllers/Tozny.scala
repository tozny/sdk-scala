package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import com.tozny.{Login, Realm}

object Tozny extends Controller with AppSecurity {
  val realm = new Realm(
    Play.current.configuration.getString("tozny.realmKeyId").get,
    Play.current.configuration.getString("tozny.realmKeySecret").get,
    Play.current.configuration.getString("tozny.apiUrl").get
  )

  def verify = LoginAction { implicit request =>
    // Access login information here via `request.user`
    Redirect("/protected")
  }

  def protectedResource = Authenticated { implicit request =>
    val toznyUser = request.user
    Ok("You are logged in as " + toznyUser.userDisplay)
  }

}
