package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import com.tozny.{Login, Realm, ToznyUser}

import scala.util.{Failure, Success, Try}

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
      case Success(u) => Ok(views.html.protectedResource(
        "Protected Resource",
        u.meta.get("displayname").getOrElse(toznyLogin.userDisplay),
        Json.prettyPrint(Json.toJson(u))
      ))
      case Failure(e) => BadRequest(e.getMessage)
    }
  }

  def logout = LogoutAction {
    Redirect(routes.Application.index)
  }
}
