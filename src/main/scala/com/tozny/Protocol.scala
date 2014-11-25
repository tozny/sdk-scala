package com.tozny

import java.net.URI
import java.nio.charset.Charset
import java.security.SecureRandom

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

import org.apache.commons.codec.binary.Base64.{decodeBase64, encodeBase64URLSafeString}
import org.apache.commons.codec.binary.Hex.{encodeHexString}
import org.apache.http.NameValuePair
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.client.{
  ClientProtocolException, HttpClient, HttpClientBuilder, HttpResponse
}
import org.apache.http.client.entity.UrlEncodedFormEntity

object Protocol {
  private val utf8 = Charset.forName("UTF-8")
  private val RANDOM_ALGORITHM = "SHA1PRNG"

  def sign(secret: String, message: String): String = {
    val secret = new SecretKeySpec(secret.getBytes(utf8), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secret)
    val result: Array[Byte] = mac.doFinal(message.getBytes(utf8))
    encodeBase64URLSafeString(result)
  }

  def verify(secret: String, signature: String, message: String): Boolean = {
    val sig = sign(secret, message)
    sig == signature  // TODO: timing insensitive comparison
  }

  // consider running in its own thread for UI responsiveness.
  def rawCall(apiUrl: String, args: List[NameValuePair]): HttpResponse = {
    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(apiUrl)
    post.setHeader("ACCEPT", "application/json")
    post.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8))
    client.execute(post)
  }

  def mkRequest(
    realmKeyId: String,
    secret: String,
    method: String,
    params: List[NameValuePair]
  ): List[NameValuePair] = {
    var req = Array(
      ("nonce",        getNonce),
      ("expires_at",   getExpires),
      ("realm_key_id", realmKeyId),
      ("method",       method)
    )
  }

  private def getNonce(): String = {
    val bytes = getRandomBytes(32)
    Hex.encodeHexString(bytes)
  }

  private def getRandomBytes(numberOfBytes: Int): Array[Byte] = {
    val bytes: Array[Byte] = new Array(numberOfBytes)
    val random = SecureRandom.getInstance(RANDOM_ALGORITHM)
    r.nextBytes(bytes)
    bytes
  }
}
