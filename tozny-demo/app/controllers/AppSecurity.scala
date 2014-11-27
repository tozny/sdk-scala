package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security._
import play.api.libs.json._
import play.api.libs.json.Json.{fromJson, toJson}
import play.api.data._
import play.api.data.Forms._
import com.tozny.{Login, Realm}

import scala.concurrent.Future

trait AppSecurity extends Controller {

  case class ToznyLoginAttempt(data: String, signature: String)

  val realm: Realm;

  val toznyForm = Form(
    mapping(
      "tozny_signed_data" -> nonEmptyText,
      "tozny_signature" -> nonEmptyText
    )(ToznyLoginAttempt.apply)(ToznyLoginAttempt.unapply)
  )

  object Authenticated extends AuthenticatedBuilder(getUserFromRequest)

  object LoginAction extends ActionBuilder[({type l[a] = AuthenticatedRequest[a, Login]})#l] {
    def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A, Login]) => Future[Result]) = {
      toznyForm.bindFromRequest()(request).fold(
        invalidForm => Future.successful(loginFailure(invalidForm.toString)),
        validForm => {
          val loginAttempt = realm.verifyLogin(validForm.data, validForm.signature)
          loginAttempt match {
            case Right(login) => {
              val req = new AuthenticatedRequest(login, request)
              block(req).map { result =>
                val json = Json.stringify(toJson(login))
                result.withSession { request.session + ("tozny_user" -> json) }
              }(executionContext)
            }
            case Left(errors) => Future.successful(loginFailure(errors.toString))
          }
        }
      )

    }
  }

  object LogoutAction extends ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
      block(request).map { result =>
        result withSession {
          request.session - "tozny_user"
        }
      }(executionContext)
    }
  }

  def getUserFromRequest(req: RequestHeader): Option[Login] = {
    for {
      json <- req.session.get("tozny_user")
      user <- Json.parse(json).asOpt[Login]
    } yield user
  }

  def loginFailure[A](e: String): Result = {
    BadRequest(e)  // TODO: proper response code
  }

}
