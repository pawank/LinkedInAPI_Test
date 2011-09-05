package code.snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import scala.xml.NodeSeq
import org.slf4j.LoggerFactory
import code.model.User


object AjaxLoginForm extends  User{
  def logger = LoggerFactory.getLogger("AjaxLoginForm")
  def render = {
      var (username,password) = ("","")
      val whence = S.referer openOr "/"

      def process(): JsCmd= {
        Thread.sleep(800)
        if (username == "") {
          logger.debug("Username is empty")
          S.error("username_error", "Testing error");
          Noop
        }
        else {
          logger.debug("Username and password are non-empty")
          S.error("username_error", "Success");
          Noop
        }

      }
      "name=username" #> SHtml.text(username, username = _, "id" -> "username") &
      "name=password" #> (SHtml.text(password, password = _, "id" -> "password") ++ SHtml.hidden(process))

    }

  def performLogin = {
        var (username,password) = ("","")
        val whence = S.referer openOr "/"

        def process(): JsCmd= {
          Thread.sleep(800)
          if (username == "") {
            logger.debug("Username is empty")
            S.error("username_error", "Testing error");
            Noop
          }
          else {
            logger.debug("Username and password are non-empty")
            val (status,msg) = User.doLogin(username, password)

          status match {
            case true =>
              S.error("username_error", msg)
              S.redirectTo("/dashboard")
            case false =>
              S.error("username_error", msg)
          }
            Noop
          }

        }
        "name=username" #> SHtml.text(username, username = _, "id" -> "username") &
        "name=password" #> (SHtml.text(password, password = _, "id" -> "password") ++ SHtml.hidden(process))

      }

}