package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import com.tozny.{Login, Realm, ToznyUser, ToznyMeta}

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
    val toznyLogin = request.user
    val user = realm.userGet(toznyLogin.userId)
    user match {
      case Right(u) => {
        val userData = Json.prettyPrint(Json.toJson(u))
        Ok("You are logged in as " + u.meta.displayname + "\n\n" + userData)
      }
      case Left(e) => BadRequest(e.toString)
    }
  }

}
