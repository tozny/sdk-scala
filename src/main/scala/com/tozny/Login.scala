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
      for {
        userId        <- (json \ "user_id")       .validate[String]
        sessionId     <- (json \ "session_id")    .validate[String]
        realmKeyId    <- (json \ "realm_key_id")  .validate[String]
        userDisplay   <- (json \ "user_display")  .validate[String]
        expiresAt     <- parseDate(json \ "expires_at")
        signatureType <- (json \ "signature_type").validate[String]
      } yield {
        Login(userId, sessionId, realmKeyId, userDisplay, expiresAt, signatureType)
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

  private def parseDate(date: JsValue): JsResult[Date] = {
    val seconds = date match {
      case JsString(str) if str.length == 10 => new JsSuccess(str.toLong)
      case JsNumber(n) => new JsSuccess(n.longValue)
      case default => JsError("error parsing date")
    }
    seconds map { (s: Long) => new Date(s * 1000) }
  }

  private def stringifyDate(date: Date): JsValue = {
    val seconds = date.getTime() / 1000L
    toJson("%d".format(seconds))
  }

}
