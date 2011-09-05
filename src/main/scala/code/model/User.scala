package code.model

import _root_.net.liftweb.record.field._
import _root_.net.liftweb.common._

import code.lib._
import net.liftweb.http.{ LiftRules, S, Req, PostRequest }
import org.slf4j.LoggerFactory
import net.liftweb.mongodb.{ DefaultMongoIdentifier, MongoDB }
import com.mongodb.BasicDBObject
import net.liftweb.util._
import net.liftweb.mongodb.record.MongoId
import net.liftweb.mongodb.record.field._
import java.util.UUID

object User extends User with MetaMegaProtoUser[User] {
  def logger = LoggerFactory.getLogger("User")

  def setPassword(email: String, password: String): Boolean = {
    User.findUserByEmail(email) match {
      case Full(u) =>
        //u.setPasswordFromListString(password :: password :: Nil)
        logger.debug("After password verification, user email is " + u.email.is + " and user ID-" + u.userIdAsString)
        import com.mongodb.{ BasicDBObject, BasicDBObjectBuilder, DBObject }
        val dbo = BasicDBObjectBuilder.start("email", email).get

        val v = User.find(dbo)
        for (ll <- v) {
          ll.setPasswordFromListString(password :: password :: Nil)
          ll.save
        }

        //import com.mongodb.{BasicDBObject, BasicDBObjectBuilder, DBObject}
        //val dbo = BasicDBObjectBuilder.start("password", u.password.is).get
        //User.update(u, dbo)

        /*
        MongoDB.use(DefaultMongoIdentifier)( db => {
          val coll = db.getCollection("mo")
          val q = new BasicDBObject("email", email)
          val o = new BasicDBObject("password", u.password.is)
          coll.update(q,o)
        })
        */
        true
      case _ =>
        false
    }
  }

  def doLogin(username: String, password: String): Tuple2[Boolean, String] = {
    val user = User.findUserByUserName(username)
    user match {
      case Full(uu) =>
        logger.debug("In login verification, username and password are " + username + " and " + password)
        val matchedpw: Boolean = uu.testPassword(Full(password))
        logger.debug("In login verification, user email is " + uu.email.is)
        if (matchedpw == true) {
          User.logUserIn(uu)
          (true, "Success")
        } else {
          logger.debug("Password saved in db - " + uu.password.is)
          (false, "Password cannot be verified for login")
        }

      case _ =>
        (false, "Unable to verify password")
    }
  }

  def register(email: String, password: String, first_name: String, last_name: String, mobile: String, country: String, city: String): Tuple2[Boolean, String] = {
    try {

      User.findUserByUserName(email) match {
        case Full(uu) =>
          return (false, "User with email as '" + email + "' already exists. Please choose another email address.<br/>")
        case _ =>
      }

      User.createRecord
        .firstName(first_name)
        .lastName(last_name)
        .email(email)
        .setPasswordFromListString(password :: password :: Nil)
        .mobile(mobile)
        .country(country)
        .city(city)
        .validated(true)
        .save
    } catch {
      case e: Exception =>
        logger.error(e.getMessage)
        return (false, e.getMessage)
    }
    (true, "")
  }

  def formLogin: LiftRules.DispatchPF = {
    case Req("form_login" :: Nil, _, PostRequest) if !User.loggedIn_? =>
      () => {
        logger.info("Doing user validation..")
        val uname = S.param("username")
        val pw = S.param("password")
        var username = ""
        uname match {
          case Full(u) =>
            username = u
          case _ =>
        }
        val user = User.findUserByUserName(username)

        user match {
          case Full(uu) =>
            val matchedpw: Boolean = uu.testPassword(pw)
            User.logUserIn(uu)
            S.notice("username_error", "Logged In")
          case _ =>
            S.error("username_error", "Unable to verify password")
        }

        S.redirectTo(S.referer openOr "/")
      }
  }

  override def valUnique(msg: String)(value: String): List[FieldError] = {
    logger.debug("Overloaded.")
    if (this.find("userName", value).isDefined)
      List(FieldError(this.userName, msg))
    else
      Nil
  }
}

class User extends MegaProtoUser[User] with MongoId[User]{
  def meta = User
 
 

  protected def valUnique(msg: String)(value: String): List[FieldError] = {

    Nil
  }
  // what's the "meta" server
  // define an additional field for a personal essay
  object textArea extends StringField(this, 2048) {
    override def displayName = "Personal Essay"

  }

  object userName extends StringField(this, "") {
     override def validations =
      meta.valUnique("Username already taken.") _ ::
        valMinLen(6, "Username cannot be smaller than 6 characters.") _ :: super.validations
  }

}
