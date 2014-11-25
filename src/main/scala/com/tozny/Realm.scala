package com.tozny

import scala.concurrent.{Await, Future, ExecutionContext}

import scala.concurrent.duration._

class Realm(
  val realmKeyId: String,
  val realmSecret: String,
  val inApiUrl: String = sys.env("API_URL")
) {

}
