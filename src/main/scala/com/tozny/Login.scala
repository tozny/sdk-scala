package com.tozny

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.Json.toJson

case class Login(
  userId:        String,
  sessionId:     String,
  realmKeyId:    String,
  userDisplay:   String,
  expiresAt:     Date,
  signatureType: String
)

object Login {

  implicit object LoginFormat extends Format[Login] {

    def reads(json: JsValue): JsResult[Login] = {
      val attempt = for {
        userId        <- (json \ "user_id")       .asOpt[String].toRight("expected user_id").right
        sessionId     <- (json \ "session_id")    .asOpt[String].toRight("expected session_id").right
        realmKeyId    <- (json \ "realm_key_id")  .asOpt[String].toRight("expected realm_key_id").right
        userDisplay   <- (json \ "user_display")  .asOpt[String].toRight("expected user_display").right
        expiresAt     <- parseDate(json \ "expires_at").toRight("expected expires_at").right
        signatureType <- (json \ "signature_type").asOpt[String].toRight("expected signature_type").right
      } yield {
        Login(userId, sessionId, realmKeyId, userDisplay, expiresAt, signatureType)
      }
      attempt match {
        case Right(login) => new JsSuccess(login)
        case Left(e)      => JsError(e)
      }
    }

    def writes(l: Login): JsValue = {
      new JsObject(Seq(
        ("user_id"        -> toJson(l.userId)),
        ("session_id"     -> toJson(l.sessionId)),
        ("realm_key_id"   -> toJson(l.realmKeyId)),
        ("user_display"   -> toJson(l.userDisplay)),
        ("expires_at"     -> stringifyDate(l.expiresAt)),
        ("signature_type" -> toJson(l.signatureType))
      ))
    }

  }

  private def parseDate(date: JsValue): Option[Date] = {
    val seconds = date match {
      case JsString(str) if str.length == 10 => Some(str.toLong)
      case JsNumber(n) => Some(n.longValue)
      case default => None
    }
    seconds map { (s: Long) => new Date(s * 1000) }
  }

  private def stringifyDate(date: Date): JsValue = {
    val seconds = date.getTime() / 1000L
    toJson("%d".format(seconds))
  }

}
