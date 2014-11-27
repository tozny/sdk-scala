package com.tozny

import java.util.Date

import org.apache.commons.codec.binary.Base64.decodeBase64

import play.api.data.validation.ValidationError
import play.api.libs.json.Json.toJson
import play.api.libs.json.{JsObject, JsValue, JsPath, Reads, Writes}

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._

class Realm(
  val realmKeyId: String,
  val realmSecret: String,
  val apiUrl: String = sys.env("API_URL")
) {

  val rawCall = Protocol.sendRequest(
    apiUrl, realmKeyId, realmSecret, _: String, _: JsObject
  )

  /**
   * We have received a signed package and signature - let's verify it.
   */
  def verifyLogin(signedData: String, signature: String): Either[Seq[ValidationError], Login] = {
    if (Protocol.checkSignature(realmSecret, signature, signedData)) {
      Protocol.decode[Login](signedData).left.map { errors =>
        errors flatMap { case (path, validationErrors) => validationErrors }
      }
    }
    else {
      Left(Seq(new ValidationError("invalid signature")))
    }
  }

  def checkValidLogin(userId: String, sessionId: String, expiresAt: Date
  ): Either[String, Boolean] = {
    val resp = rawCall("realm.check_valid_login", new JsObject(Seq(
      "user_id" -> toJson(userId),
      "session_id" -> toJson(sessionId),
      "expires_at" -> toJson(Protocol.encodeTime(expiresAt))
    )))
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
  def questionChallenge[A,B](
    question: A, userId: String
  )(implicit w: Writes[A], r: Reads[B]): Either[String, B] = {
    val params = new JsObject(Seq("question" -> toJson(question)))
    for {
      json <- rawCall("realm.question_challenge", params).right
      result <- json.asOpt.toRight("error parsing response").right
    } yield result
  }

  def userGet(userId: String): Either[Seq[ValidationError], ToznyUser] = {
    val resp = rawCall("realm.user_get", new JsObject(Seq(
      "user_id" -> toJson(userId)
    )))
    for {
      r    <- resp.left.map(err).right
      user <- ToznyUser.ToznyUserFormat.reads(r \ "results").asEither.left.map(flatErrors).right
    } yield user

      /* Left(Seq(new ValidationError("invalid signature"))) */
  }

  private def err(e: String): Seq[ValidationError] = {
    Seq(ValidationError(e))
  }

  private def flatErrors(es: Seq[(JsPath, Seq[ValidationError])]): Seq[ValidationError] = {
    es flatMap { case (p, vs) => vs }
  }

}
