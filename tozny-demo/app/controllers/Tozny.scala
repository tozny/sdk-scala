package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import com.tozny.{Login, Realm, ToznyUser}

object Tozny extends Controller with AppSecurity {
  val realm = new Realm(
    Play.current.configuration.getString("tozny.realmKeyId").get,
    Play.current.configuration.getString("tozny.realmKeySecret").get,
    Play.current.configuration.getString("tozny.apiUrl").get
  )

  def verify = LoginAction { implicit request =>
    // Access login information here via `request.user`
    Redirect(routes.Tozny.protectedResource)
  }

  def protectedResource = Authenticated { implicit request =>
    val toznyLogin = request.user
    val user = realm.userGet(toznyLogin.userId)
    user match {
      case Right(u) => Ok(views.html.protectedResource(
        "Protected Resource",
        u.meta.get("displayname").getOrElse(toznyLogin.userDisplay),
        Json.prettyPrint(Json.toJson(u))
      ))
      case Left(e) => BadRequest(e.toString)
    }
  }

  def logout = LogoutAction {
    Redirect(routes.Application.index)
  }
}
