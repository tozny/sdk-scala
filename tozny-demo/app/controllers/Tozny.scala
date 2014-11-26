package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import com.tozny.Realm


object Tozny extends Controller {

  val realm = new Realm(
    Play.current.configuration.getString("tozny.realmId").get,
    Play.current.configuration.getString("tozny.realmKey").get,
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
        val verificationResult = realm.verifyLogin[JsValue](validForm.data, validForm.signature)
        verificationResult match {
          case Right(json) => Ok(json)
          case x => BadRequest(x.toString)  // TODO: proper response code
        }
      }
    )
  }
}
