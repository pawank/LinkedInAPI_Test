package code.snippet

import org.slf4j.LoggerFactory
import scala.xml.NodeSeq
import code.model.User
import net.liftweb.common.Full
import net.liftweb.util.BindHelpers._
import net.liftweb.http._
class Setup {
  def logger = LoggerFactory.getLogger("Setup")


  def linkTracking(html: NodeSeq): NodeSeq = {
    <span>Tracking URL</span>
  }

  def linkEmails(html: NodeSeq): NodeSeq = {
    <span>Email</span>
  }

  def linkSocial(html: NodeSeq): NodeSeq = {
    <span>Social</span>
  }

  def render(html: NodeSeq): NodeSeq = {
    logger.info("Rendering Setup snippet....")
    var msg = <p></p>
    if (User.loggedIn_?) {
      User.currentUser match {
        case Full(u) =>
          msg = <b>Welcome, {u.firstName.is}{u.lastName.is} </b>
        case _ =>
      }

    }
    else
      S.redirectTo("/")


    bind("op", html,
      "tracking" -> SHtml.link("/tracking", () => linkTracking(html), <img src="/classpath/ui/assets/images/setup/url_block.jpg" class="setup"/>),
      "emails" -> SHtml.link("/emails", () => linkEmails(html), <img src="/classpath/ui/assets/images/setup/emails_block.jpg" class="setup"/>),
      "social" -> SHtml.link("/social", () => linkSocial(html), <img src="/classpath/ui/assets/images/setup/social_networks_block.jpg" class="setup"/>),
      "message" -> msg
    )
  }
}
