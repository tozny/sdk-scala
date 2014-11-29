package com.tozny

import java.util.Date
import play.api.libs.json._
import play.api.libs.json.Json.toJson

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

  type ToznyMeta = Map[String, String]

  implicit object ToznyUserFormat extends Format[ToznyUser] {

    def reads(json: JsValue): JsResult[ToznyUser] = {
      val user = for {
        userId            <- (json \ "user_id")            .asOpt[String].toRight("expected user_id").right
        status            <- (json \ "status")             .asOpt[String].toRight("expected status").right
        created           <- parseDate(json \ "created")   .toRight("error parsing 'created'").right
        meta              <- (json \ "meta")               .asOpt[ToznyMeta].toRight("expected meta").right
      } yield {
        val lastLogin         = parseDate(json \ "last_login")
        val lastFailedLogin   = parseDate(json \ "last_failed_login")
        val modified          = parseDate(json \ "modified").getOrElse(created)
        val blocked           = parseInt(json \ "blocked")
        val loginAttempts     = parseInt(json \ "login_attempts")
        val totalLogins       = parseInt(json \ "total_logins")
        val totalFailedLogins = parseInt(json \ "total_failed_logins")
        val totalDevices      = parseInt(json \ "total_devices")
        ToznyUser(
          userId,
          blocked,
          loginAttempts,
          status,
          created,
          modified,
          lastLogin,
          totalLogins,
          totalFailedLogins,
          lastFailedLogin,
          totalDevices,
          meta
        )
      }
      user match {
        case Right(u) => new JsSuccess(u)
        case Left(e)  => JsError(e)
      }
    }

    def writes(u: ToznyUser): JsValue = {
      val lastLogin = u.lastLogin.map(stringifyDate).getOrElse(JsNull)
      val lastFailedLogin = u.lastFailedLogin.map(stringifyDate).getOrElse(JsNull)
      new JsObject(Seq(
        "user_id"             -> toJson(u.userId),
        "blocked"             -> toJson(u.blocked),
        "login_attempts"      -> toJson(u.loginAttempts),
        "status"              -> toJson(u.status),
        "created"             -> toJson(stringifyDate(u.created)),
        "modified"            -> toJson(stringifyDate(u.modified)),
        "last_login"          -> lastLogin,
        "total_logins"        -> toJson(u.totalLogins),
        "total_failed_logins" -> toJson(u.totalFailedLogins),
        "last_failed_login"   -> lastFailedLogin,
        "total_devices"       -> toJson(u.totalDevices),
        "meta"                -> toJson(u.meta)
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

  private def parseInt(n: JsValue): Int = n match {
    case JsString(str) => try { str.toInt } catch { case e:NumberFormatException => 0 }
    case JsNumber(num) => num.intValue
    case default => 0
  }

  private def stringifyDate(date: Date): JsValue = {
    val seconds = date.getTime() / 1000L
    toJson("%d".format(seconds))
  }

}
