package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import com.tozny.Realm

trait AppSecurity extends Controller {

  case class ToznyUser(id: String, displayName: String)

  object Authenticated extends AuthenticatedBuilder(getUserFromRequest)

  def getUserFromRequest(req: RequestHeader): Option[ToznyUser] = {
    for {
      userId      <- req.session.get("tozny.user_id")
      displayName <- req.session.get("tozny.display_name")
    } yield ToznyUser(userId, displayName)
  }
}
