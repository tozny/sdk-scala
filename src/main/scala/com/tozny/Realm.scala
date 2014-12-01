package com.tozny

import java.util.Date

import org.apache.commons.codec.binary.Base64.decodeBase64

import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsObject, JsValue, Reads, Writes}

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class Realm(
  val realmKeyId: String,
  val realmSecret: String,
  val apiUrl: String = sys.env("API_URL")
) {
  import Protocol.asTry

  val rawCall = Protocol.sendRequest(
    apiUrl, realmKeyId, realmSecret, _: String, _: JsObject
  )

  /**
   * We have received a signed package and signature - let's verify it.
   */
  def verifyLogin(signedData: String, signature: String): Try[Login] = {
    if (Protocol.checkSignature(realmSecret, signature, signedData)) {
      Protocol.decode[Login](signedData)
    }
    else {
      new Failure(InvalidSignature("invalid signature"))
    }
  }

  def checkValidLogin(userId: String, sessionId: String, expiresAt: Date
  ): Try[Boolean] = {
    val resp = rawCall("realm.check_valid_login", new JsObject(Seq(
      "user_id" -> toJson(userId),
      "session_id" -> toJson(sessionId),
      "expires_at" -> toJson(Protocol.encodeTime(expiresAt))
    )))
    for {
      r   <- resp
      ret <- asTry((r \ "return").validate[String])
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
  def questionChallenge[A,B](
    question: A, userId: String
  )(implicit w: Writes[A], r: Reads[B]): Try[B] = {
    val params = new JsObject(Seq("question" -> toJson(question)))
    for {
      json <- rawCall("realm.question_challenge", params)
      result <- asTry(json.validate[B])
    } yield result
  }

  def userGet(userId: String): Try[ToznyUser] = {
    val resp = rawCall("realm.user_get", new JsObject(Seq(
      "user_id" -> toJson(userId)
    )))
    for {
      r    <- resp
      user <- asTry((r \ "results").validate[ToznyUser])
    } yield user
  }

}
