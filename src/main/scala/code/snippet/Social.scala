package code.snippet

import org.slf4j.LoggerFactory
import scala.xml.NodeSeq
import code.model.User
import net.liftweb.common.Full
import net.liftweb.util.BindHelpers._
import net.liftweb.http._
import code.linkedin.LinkedInConnect

class Social {
  def logger = LoggerFactory.getLogger("Social")
  object LoginSession {
    val showLogin = false
  }


  def connectLinkedIn(html: NodeSeq): NodeSeq = {
    val oauth_token = S.param("oauth_token").openOr("")
    val oauth_verifier = S.param("oauth_verifier").openOr("")
    connectAndFetchDataFromLinkedIn(oauth_token, oauth_verifier,"")
    //fetchDataFromLinkedIn
  }

  def connectAndFetchDataFromLinkedIn(oauth_token: String, oauth_verifier: String, api_url:String): NodeSeq = {
    logger.debug("Connecting to LinkedIn...")
    var data = ""

    val (token, needsRedirect) = LinkedInConnect.authenticate(oauth_token, oauth_verifier)
    if (needsRedirect) {
      val url = LinkedInConnect.getLinkedInAuthorizationUrl(token)
      logger.debug("Action is to redirect to LinekdIn oauth link - " + url)
      //JsCmds.RedirectTo(url)
      S.redirectTo(url)
      logger.debug("This should not appear in log..")
    } else {
      logger.debug("Oauth verifier returned from LinkedIn is " + oauth_verifier)
      data = LinkedInConnect.processRequest(token, api_url)
    }
    if (token != null)
      S.set("linkedin_session_valid", "1")

    logger.info("Sending response XML back to calling prorgam as [" + data + "]")
    <data>
      {data}
    </data>
  }

  def linkedin(html: NodeSeq): NodeSeq = {
    logger.info("Connecting to LinkedIn from dashboard...")
    bind("in", html,
      //"login" -> SHtml.link("dashboard",() => connectLinkedIn(html) , Text("Login with LinkedIn"))
      "login" -> SHtml.link("/social", () => connectLinkedIn(html), <img src="/classpath/ui/assets/images/log-in-linkedin-large.png"/>)
    )
  }


  def checkAccess: NodeSeq = {
    if (User.loggedIn_?) {
      User.currentUser match {
        case Full(u) =>
          <span id="message">Welcome back,
            {u.firstName.is}{u.lastName.is}
          </span>
        case _ =>
          <span id="message">Welcome back</span>
      }

    }
    else
      S.redirectTo("/")
  }

  def render(html: NodeSeq): NodeSeq = {
    if (LoginSession.showLogin) {
    if (User.loggedIn_?) {
      User.currentUser match {
        case Full(u) =>
          "#message *" #> <b>Welcome,
            {u.firstName.is}{u.lastName.is}
          </b>
        case _ =>
          "#message *" #> "Welcome"
      }

    }
    else
      S.redirectTo("/")
    }

    //logger.debug("Saving user profile in Social.scala..")

    var node: NodeSeq = <p></p>
    val oauth_token = S.param("oauth_token").openOr("")
    val oauth_verifier = S.param("oauth_verifier").openOr("")
    if ((oauth_token != "") && (oauth_verifier != "")) {
      logger.debug("Value of LinkedIn oauth verifier before processing is - " + oauth_verifier)
      node = connectAndFetchDataFromLinkedIn(oauth_token, oauth_verifier,"http://api.linkedin.com/v1/people/~:(id,first-name,last-name,headline,location:(name),industry,num-connections,summary,specialties,interests,skills,educations,phone-numbers,date-of-birth,main-address,picture-url,distance,api-public-profile-request:(url),site-public-profile-request:(url),api-standard-profile-request:(headers),public-profile-url,three-current-positions,three-past-positions)")
    	LinkedInConnect.saveProfile("pawan.kumar@gmail.com",node.toString)
      //processOAuth(oauth_verifier)
      logger.debug("Response XML is set as content for ID and the data is " + node.toString())
      S.redirectTo("/audience")
    }
    else {
      if (S.get("linkedin_session_valid").openOr("") == "1") {
        logger.info("LinkedIn session is still valid")
        //NOTE:Open for actual testing
        //node = connectAndFetchDataFromLinkedIn(oauth_token, oauth_verifier, Constants.LINKEDIN_CONNECTIONS)
        logger.debug("For valid LinkedIn session, response XML is set as content for ID with value - " + node.toString())
      }
    }

    bind("in", html,
      //"login" -> SHtml.link("dashboard",() => connectLinkedIn(html) , Text("Login with LinkedIn"))
      "login" -> SHtml.link("/social", () => connectLinkedIn(html), <img src="/classpath/ui/assets/images/log-in-linkedin-large.png"/>),
      "data" -> node
    )
  }
}
