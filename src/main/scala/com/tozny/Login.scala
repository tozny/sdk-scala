package com.tozny

import java.util.Date

import play.api.libs.json.Json.toJson
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._ // Combinator syntax

case class Login(
  userId:        String,
  sessionId:     String,
  realmKeyId:    String,
  userDisplay:   String,
  expiresAt:     Date,
  signatureType: String
)

object Login {

  val readsDate: Reads[Date] = {
    val str = (minLength[String](10) keepAnd minLength[String](10)).map(_.toLong)
    val num = Reads.of[Long]
    (str or num).map { (seconds: Long) =>
      new Date(seconds * 1000)
    }
  }

  val writesDate: Writes[Date] = Writes { date =>
    val seconds = date.getTime() / 1000L
    toJson("%d".format(seconds))
  }

  val formatDate: Format[Date] = Format(readsDate, writesDate)

  implicit val loginFormat: Format[Login] = (
    (__ \ "user_id")       .format[String]     and
    (__ \ "session_id")    .format[String]     and
    (__ \ "realm_key_id")  .format[String]     and
    (__ \ "user_display")  .format[String]     and
    (__ \ "expires_at")    .format(formatDate) and
    (__ \ "signature_type").format[String]
  )(Login.apply _, unlift(Login.unapply))

}
