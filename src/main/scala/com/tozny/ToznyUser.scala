package com.tozny

import java.util.Date

import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.json.Writes._
import play.api.libs.functional.syntax._ // Combinator syntax

case class ToznyUser(
  userId:            String,
  blocked:           Int,
  loginAttempts:     Int,
  status:            String,
  created:           Date,
  modified:          Date,
  lastLogin:         Option[Date],
  totalLogins:       Int,
  totalFailedLogins: Int,
  lastFailedLogin:   Option[Date],
  totalDevices:      Int,
  meta:              ToznyUser.ToznyMeta
)

object ToznyUser {
  import Login.formatDate

  type ToznyMeta = Map[String, String]

  val optDate: Format[Option[Date]] = Format.optionWithNull(formatDate)
  val intOrZeroReads: Reads[Int] = {
    val num = Reads.of[Int]
    val str = Reads.of[String] map { s =>
      try { s.toInt } catch { case e:NumberFormatException => 0 }
    }
    num or str or Reads.pure(0)
  }
  val intOrZero: Format[Int] = Format(intOrZeroReads, Writes.of[Int])

  implicit val toznyUserFormat: Format[ToznyUser] = (
    (__ \ "user_id")              .format[String]        and
    (__ \ "blocked")              .format(intOrZero)     and
    (__ \ "login_attempts")       .format(intOrZero)     and
    (__ \ "status")               .format[String]        and
    (__ \ "created")              .format(formatDate)    and
    (__ \ "modified")             .format(formatDate)    and
    (__ \ "last_login")           .format(optDate)       and
    (__ \ "total_logins")         .format(intOrZero)     and
    (__ \ "total_failed_logins")  .format(intOrZero)     and
    (__ \ "last_failed_login")    .format(optDate)       and
    (__ \ "total_devices")        .format(intOrZero)     and
    (__ \ "meta")                 .format[ToznyMeta]
  )(ToznyUser.apply _, unlift(ToznyUser.unapply))

}
