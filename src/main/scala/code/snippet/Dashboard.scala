package code.snippet

import org.slf4j.LoggerFactory
import scala.xml.{ NodeSeq, Text }
import net.liftweb.util._
import code.model.User
import net.liftweb.common.Full
import net.liftweb.util.BindHelpers._
import org.scribe.oauth.OAuthService
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.LinkedInApi
import org.scribe.model._
import net.liftweb.http._
import code.linkedin.LinkedInConnect
import js.JsCmds
import code.utils.Constants

class Dashboard {
  def logger = LoggerFactory.getLogger("Dashboard")

  def render(html: NodeSeq): NodeSeq = {
    var msg = <p></p>
    if (User.loggedIn_?) {
      User.currentUser match {
        case Full(u) =>
          msg = <b>Welcome,{ u.firstName.is }{ u.lastName.is }</b>
        case _ =>
      }

    } else
      S.redirectTo("/")

    bind("db", html,
      "message" -> msg)
  }
}
