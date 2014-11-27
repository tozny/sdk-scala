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

  case class ToznyLoginAttempt(data: String, signature: String)

  val toznyForm = Form(
    mapping(
      "tozny_signed_data" -> nonEmptyText,
      "tozny_signature" -> nonEmptyText
    )(ToznyLoginAttempt.apply)(ToznyLoginAttempt.unapply)
  )


  def verify = Action { implicit request =>
    toznyForm.bindFromRequest.fold(
      invalidForm => BadRequest(invalidForm.toString),
      validForm => {
        val loginAttempt = realm.verifyLogin(validForm.data, validForm.signature)
        loginAttempt match {
          case Right(login) => Ok(login.toString).withSession(
            request.session +
            ("tozny.user_id" -> login.userId) +
            ("tozny.display_name" -> login.userDisplay)
          )
          case Left(errors) => BadRequest(errors.toString)  // TODO: proper response code
        }
      }
    )
  }

  def protectedResource = Authenticated { implicit request =>
    val toznyUser = request.user
    Ok("You are logged in as " + toznyUser.displayName)
  }

}
