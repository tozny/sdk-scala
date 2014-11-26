package controllers

import org.apache.commons.codec.binary.Base64
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import play.api.libs.json.DefaultReads

object Tozny extends Controller with DefaultReads {
  case class ToznyLoginAttempt ( tozny_signed_data:String, tozny_signature:String)
  val form = Form(
    mapping(
      "tozny_signed_data" -> text,
      "tozny_signature" -> text
    )(ToznyLoginAttempt.apply)(ToznyLoginAttempt.unapply)
  )

  def verify = Action { implicit request =>
    form.bindFromRequest.fold(
      invalid_attempt => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.index("Invalid attempt", "Error with form", ""))
      },
      valid_attempt => {
        // TODO
        val realm = new com.tozny.Realm(
          "sid_54738a875dd7b",
          "dc3d1f7c855a570ec24aa768fe4f02f00eda0b3387adace9462b10639448e684",
          "https://api.tozny.com/index.php"
        )

        realm.verifyLogin[JsValue](valid_attempt.tozny_signed_data, valid_attempt.tozny_signature) match {
          case Left(error) =>
            BadRequest(views.html.index("Invalid signature", "Error with your signature:  "+error, ""))
          case Right(verify_response:JsValue) =>
            realm.userGet[JsValue]((verify_response \ "user_id").toString) match {
              case Left(error) =>
                BadRequest(views.html.index("Invalid user record", "Error with your user record:  "+error, ""))
              case Right(user_response:JsValue) =>
                Ok("ok, I received POST data. That's all...: "+user_response.toString());
            }
        }
      }
    )
  }
}
