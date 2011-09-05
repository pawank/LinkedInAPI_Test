package code.snippet

import net.liftweb._
import http._
import js.JE._
import util.Helpers._
import js._
import JsCmds._
import org.slf4j.LoggerFactory
import code.model.User
import java.lang.Boolean
import xml.{Group, NodeSeq, Text}
import net.liftweb.http.js.jquery.JqJE._
import net.liftweb.http.SHtml._

class LostPassword {
  def logger = LoggerFactory.getLogger("LostPassword")

  def checkFields(u: String)(p: String): Tuple2[Boolean, String] = {
    logger.info("Both passwords are (" + u + "," + p + ")")
    if ((u == "") && (p == ""))
      (false, "Both password fields are empty")
    else if (u != p)
      (false, "Passwords do not match")
    else if (u == p)
      (true, "")
    else (false, "Unknown error")
  }

  def render(html: NodeSeq): NodeSeq = {
    html
  }

  def performReset = {
    //val email = S.param("email") openOr ""
    var (email, confirm_password, password) = ("", "", "")
    val whence = S.referer openOr "/"

    def process(): JsCmd = {
      Thread.sleep(800)
      val chkstatus = checkFields(password)(confirm_password)
      if (!chkstatus._1) {
        logger.debug(chkstatus._2)
        S.error("message", chkstatus._2);
        Noop
      }
      else {
        logger.debug("Confirm_password and password are non-empty for email - " + email)
        val status = User.setPassword(email, password)
        logger.debug("Does password reset done for email - " + email)
        status match {
          case true =>
            S.notice("message", "Password reset is successful")
            S.redirectTo("/")
          case false =>
            S.error("message", "Password cannot be reset. Please contact us at +91-8792136179")
        }
        Noop
      }
    }

    "name=email" #> SHtml.text(email, email = _) &
      "name=password" #> SHtml.password(password, password = _, "placeholder" -> "Enter your password") &
      "name=confirm_password" #> (SHtml.password(confirm_password, confirm_password = _, "placeholder" -> "Enter your confirmation password") ++ SHtml.hidden(process))
  }
}
