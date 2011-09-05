package code
package snippet

import net.liftweb._
import http._
import common._
import util.Helpers._
import js._
import JsCmds._
import JE._
import scala.xml.NodeSeq
import org.slf4j.LoggerFactory

/**
 * Ajax for processing... it looks a lot like the Stateful example
 */
object AjaxExample {
  def logger = LoggerFactory.getLogger("AjaxExample")
  def render = {
    // state
    var username = ""
    var password = "0"
    val whence = S.referer openOr "/"

    // our process method returns a
    // JsCmd which will be sent back to the browser
    // as part of the response
    def process(): JsCmd= {

      // sleep for 400 millis to allow the user to
      // see the spinning icon
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
}
