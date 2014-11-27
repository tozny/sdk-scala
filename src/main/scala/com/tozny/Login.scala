package com.tozny

import java.util.Date
import play.api.libs.json._

case class Login(
  userId:        String,
  sessionId:     String,
  realmKeyId:    String,
  userDisplay:   String,
  expiresAt:     Date,
  signatureType: String
)

object Login {

  implicit object LoginReads extends Reads[Login] {
    def reads(json: JsValue): JsResult[Login] = {
      val attempt = for {
        userId        <- (json \ "user_id")       .asOpt[String].toRight("expected user_id").right
        sessionId     <- (json \ "session_id")    .asOpt[String].toRight("expected session_id").right
        realmKeyId    <- (json \ "realm_key_id")  .asOpt[String].toRight("expected realm_key_id").right
        userDisplay   <- (json \ "user_display")  .asOpt[String].toRight("expected user_display").right
        expiresAt     <- (json \ "expires_at")    .asOpt[Long]  .toRight("expected expires_at").right
        signatureType <- (json \ "signature_type").asOpt[String].toRight("expected signature_type").right
      } yield {
        val expires = new Date(expiresAt * 1000)
        Login(userId, sessionId, realmKeyId, userDisplay, expires, signatureType)
      }
      attempt match {
        case Right(login) => new JsSuccess(login)
        case Left(e)      => JsError(e)
      }
    }
  }

}
