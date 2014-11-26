package com.tozny

import scala.collection.JavaConversions._
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.Date

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

import org.apache.commons.codec.binary.Base64.{
  decodeBase64, encodeBase64URLSafeString
}
import org.apache.commons.codec.binary.Hex
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.{HttpResponse, NameValuePair}
import org.apache.http.message.BasicNameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.util.EntityUtils

import play.api.libs.json.{Json, JsObject, JsValue, Reads, Writes}
import play.api.libs.json.Json.{toJson}

object Protocol {
  private val utf8 = Charset.forName("UTF-8")
  private val RANDOM_ALGORITHM = "SHA1PRNG"

  def sign(secret: String, message: String): String = {
    val secretKey = new SecretKeySpec(secret.getBytes(utf8), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val result: Array[Byte] = mac.doFinal(message.getBytes(utf8))
    encodeBase64URLSafeString(result)
  }

  def checkSignature(secret: String, signature: String, message: String): Boolean = {
    val sig = sign(secret, message)
    sig == signature  // TODO: timing insensitive comparison
  }

  def sendRequest(
    apiUrl: String, realmKeyId: String, secret: String, method: String,
    params: JsObject
  ): Either[String, JsValue] = {
    val payload = mkRequest(realmKeyId, secret, method, params)
    val resp = rawCall(apiUrl, payload)
    if (resp.getStatusLine.getStatusCode >= 300) {
      Left(resp.getStatusLine.getReasonPhrase)
    }
    else {
      Right(Json.parse(EntityUtils.toString(resp.getEntity)))
    }
  }

  // consider running in its own thread for UI responsiveness.
  def rawCall(apiUrl: String, args: Iterable[NameValuePair]): HttpResponse = {
    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(apiUrl)
    post.setHeader("ACCEPT", "application/json")
    post.setEntity(new UrlEncodedFormEntity(asJavaIterable(args), utf8))
    client.execute(post)
  }

  //rawCall("https://api.tozny.com/index.php",args)
  def mkRequest(
    realmKeyId: String,
    secret: String,
    method: String,
    params: JsObject
  ): Iterable[NameValuePair] = {
    val meta = new JsObject(Seq(
      "nonce" ->  toJson(getNonce),
      "expires_at" ->  toJson(getExpires),
      "realm_key_id" -> toJson(realmKeyId),
      "method" ->  toJson(method)
    ))
    val payload = params ++ meta
    val json = Json.stringify(payload)
    val encoded = encodeBase64URLSafeString(json.getBytes(utf8))
    val signature = sign(secret, encoded)
    return List(
      new BasicNameValuePair("signed_data", encoded),
      new BasicNameValuePair("signature", signature)
    )
  }

  def decode[A](payload: String)(implicit r: Reads[A]): Option[A] = {
    val decoded = decodeBase64(payload)
    Json.parse(new String(decoded, utf8)).asOpt
  }

  def encodeTime(date: Date): String = {
    val inSeconds = Math.floor(date.getTime() / 1000)
    return inSeconds.toString
  }

  private def getExpires(): String = {
    Math.floor((System.currentTimeMillis() + (5 * 60 * 1000)) / 1000).toString
  }

  private def getNonce(): String = {
    val bytes = getRandomBytes(32)
    Hex.encodeHexString(bytes)
  }

  private def getRandomBytes(numberOfBytes: Int): Array[Byte] = {
    val bytes: Array[Byte] = new Array(numberOfBytes)
    val random = SecureRandom.getInstance(RANDOM_ALGORITHM)
    random.nextBytes(bytes)
    bytes
  }
}
