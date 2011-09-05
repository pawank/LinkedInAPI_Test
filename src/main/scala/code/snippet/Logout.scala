package code.snippet

import code.model.User
import net.liftweb.http.S
import net.liftweb.util.BindHelpers._
import xml.{Text, NodeSeq}
import net.liftweb.http.SHtml._

object Logout {
  def render(html:NodeSeq):NodeSeq = {

    bind("logout", html, "logoff" -> a(() => {
      User.logoutCurrentUser;
      S.redirectTo("/")}, html)
    )


  }
}