package com.tozny

import scala.collection.JavaConversions._
import java.nio.charset.Charset
import java.security.SecureRandom

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

import org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString
import org.apache.commons.codec.binary.Hex
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.protocol.HTTP
import org.apache.http.{HttpResponse, NameValuePair}

import org.apache.http.client.entity.UrlEncodedFormEntity

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

  // consider running in its own thread for UI responsiveness.
  def rawCall(apiUrl: String, args: List[NameValuePair]): HttpResponse = {
    val client = HttpClientBuilder.create().build()
    val post = new HttpPost(apiUrl)
    post.setHeader("ACCEPT", "application/json")
    post.setEntity(new UrlEncodedFormEntity(args, HTTP.UTF_8))
    client.execute(post)
  }

  //rawCall("https://api.tozny.com/index.php",args)
  def mkRequest(
    realmKeyId: String,
    secret: String,
    method: String,
    params: List[NameValuePair]
  ): List[NameValuePair] = {
    List(
      "nonce" ->  getNonce,
      "expires_at" ->  getExpires,
      "realm_key_id" -> realmKeyId,
      "method" ->  method
    ).map { case (k,v) ⇒
      new NameValuePair {
        override def getName: String = k
        override def getValue: String = v
      }
    }
  }

  private def getExpires(): String = {
    ???
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
