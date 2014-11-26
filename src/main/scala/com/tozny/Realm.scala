package com.tozny

import java.util.Date

import org.apache.commons.codec.binary.Base64.decodeBase64

import play.api.libs.json.JsValue

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._

class Realm(
  val realmKeyId: String,
  val realmSecret: String,
  val apiUrl: String = sys.env("API_URL")
) {

  val rawCall = Protocol.sendRequest(
    apiUrl, realmKeyId, realmSecret, _: String, _: Map[String, String]
  )

  /**
   * We have received a signed package and signature - let's verify it.
   */
  def verifyLogin(signedData: String, signature: String): Either[String, JsValue] = {
    if (Protocol.checkSignature(realmSecret, signature, signedData)) {
      Right(Protocol.decode(signedData))
    }
    else {
      Left("invalid signature")
    }
  }

  def checkValidLogin(userId: String, sessionId: String, expiresAt: Date
  ): Either[String, Boolean] = {
    val resp = rawCall("realm.check_valid_login", Map(
      "user_id" -> userId,
      "session_id" -> sessionId,
      "expires_at" -> Protocol.encodeTime(expiresAt)
    ))
    for {
      r   <- resp.right
      ret <- (r \ "return").asOpt[String].toRight("response has no field 'return'").right
    }
    yield ret == "true"
  }

  /**
   * Sends a question challenge - optionally directed to a specific user.
   *
   * Fields returned with the response include:
   * - challenge
   * - realm_key_id
   * - session_id,
   * - qr_url
   * - mobile_url
   * - created_at
   * - presence
   */
  /* def questionChallenge() */

}
