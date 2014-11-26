package controllers

import org.apache.commons.codec.binary.Base64
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.DefaultReads.JsValueReads

object Tozny extends Controller {
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
        BadRequest(views.html.index("Invalid attempt", "Error with form"))
      },
      valid_attempt => {
        // TODO
        val realm = new com.tozny.Realm("sid_54738a875dd7b", "dc3d1f7c855a570ec24aa768fe4f02f00eda0b3387adace9462b10639448e684", "https://api.tozny.com/index.php")

        realm.verifyLogin(valid_attempt.tozny_signed_data, valid_attempt.tozny_signature) match {
          case Left(error) =>
            BadRequest(views.html.index("Invalid signature", "Error with your signature:  "+error+ " "+(Json.parse(new String(new Base64(true).decode(valid_attempt.tozny_signed_data)))).getClass().getName))
          case Right(json_response) =>
            Ok("ok, I recived POST data. That's all...: tozny_signed_data: " + valid_attempt.tozny_signed_data + "; tozny_signature: " +valid_attempt.tozny_signature);
        }
      }
    )
  }
}
