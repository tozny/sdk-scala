package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._


object Tozny extends Controller {

  def verify = Action { request =>
    val body: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())

    (body.get("tozny_signed_data"), body.get("tozny_signature")) match {

      case (Some(val1), Some(val2)) => 
        val jsonString = s"""  { "status" : "Successful signin!", "tozny_signed_data" : "${val1(0)}", "tozny_signature" : "${val2(0)}" }  """
        Ok(Json.parse(jsonString))
      
      case _ => BadRequest( request.body.toString )

    }
  }
}
