package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import com.tozny.Realm


object Tozny extends Controller {

  val realm = new Realm(
    Play.current.configuration.getString("tozny.realmId").get,
    Play.current.configuration.getString("tozny.realmKey").get,
    Play.current.configuration.getString("tozny.apiUrl").get
  )

  def verify = Action { request =>
    val body: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())

    (body.get("tozny_signed_data"), body.get("tozny_signature")) match {

      case (Some(data), Some(signature)) => 
        val result = realm.verifyLogin[JsValue](data(0), signature(0))
        result match {
          case Right(json) => Ok( json )
          case x => BadRequest(x.toString)  // TODO: proper response code
        }
      
      case _ => BadRequest( request.body.toString )

    }
  }
}
