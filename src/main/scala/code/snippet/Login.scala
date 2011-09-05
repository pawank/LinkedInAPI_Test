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
import code.utils.Mail


object Login extends User {
  def logger = LoggerFactory.getLogger("Login")
  
 
def render(html:NodeSeq):NodeSeq = {
    val whence = S.referer openOr "/"
    if (whence == "/dashboard")
      S.notice("message","You have been successfully logged-out")
    html
  }

  def checkFields(u: String)(p: String): Tuple2[Boolean,String] = {
    logger.info("Both passwords are (" + u + "," + p + ")")
    if ((u == "") && (p == ""))
      (false,"Both username and password are empty")
    else if ((u == "") && (p != ""))
      (false,"Username is empty")
    else if ((u != "") && (p == ""))
      (false,"Password is empty")
    else
      (true,"")
  }

  // get the id of some elements to update
  val spanName: String = S.attr("id_name") openOr "forgot_password_link"
  val msgName: String = S.attr("id_msg") openOr "message"


  val forgot_passwordform: NodeSeq =
    <div>
      <input class="login" type="text" name="email" id="email" placeholder="Enter your email address"></input>
      <input type="submit" class="lift:Login.performLostPassword" value="Send me password"></input>
    </div>


  def forgot_password(html: NodeSeq): NodeSeq = {
    a(() => {
      logger.info("Show form to the user for entering email");
      Replace("message", <div id="message"></div>) & SetHtml("lost_password_id", forgot_passwordform)
    },
      html)
  }


  def forgotpassword(html: NodeSeq): NodeSeq = {
    logger.info("Checking lost password of the user..")

      bind("login", html,"forgot_password" -> forgot_password _)
  }

  //Check username/password and authenticate a user
  //On success, redirect to dashboard/my home, else show error message and redirect to the page from where user has come
  def performLogin = {
    var (username, password) = ("", "")
    val whence = S.referer openOr "/"

    def process(): JsCmd = {
      //Thread.sleep(800)
      val chkstatus = checkFields(username)(password)
      if (!chkstatus._1) {
        logger.debug(chkstatus._2)
        S.error("message", chkstatus._2);
        Noop
      }
      else {
        logger.debug("Username and password are non-empty")
        val (status, msg) = User.doLogin(username, password)
        status match {
          case true =>
            S.notice("message", msg)
            S.redirectTo("/dashboard")
          case false =>
            S.error("message", msg)
        }
        Noop
      }
    }

    "name=username" #> SHtml.text(username, username = _, "id" -> "username", "placeholder" -> "Enter your username") &
      "name=password" #> (SHtml.password(password, password = _, "id" -> "password", "placeholder" -> "Enter your password") ++ SHtml.hidden(process))
  }

  def checkLostPassword(email: String): JsCmd = {
    logger.info("Creating new password for the user and sending re-activating email..");

    Mail.sendEmail("pawan@efoundry.in",email,"Password re-activation email",
      <p>Please reset your password by clicking below link<br/>http://mo.efoundry.in/lostpassword?email={email}</p>,false
    )

    //val v = "Password activation email has been sent to your email address - '" + email + "'"
    val v = <i>Password activation email has been sent to your email address - '{email}'</i>
    //SetHtml("message", <i>Password activation email has been sent to your email address - '{email}'</i>)
    //JsRaw("document.getElementById('lost_password_id').innerHTML='';document.getElementById('message').innerHTML=\"" + v + "\"")
    Replace("lost_password_id", <div id="lost_password_id"></div>) & SetHtml("message", <i>Password activation email has been sent to your email address - '{email}'</i>)

  }

  def performLostPassword = {
    import js.JE._
    "* [onclick]" #> SHtml.ajaxCall(ValById("email"), s => checkLostPassword(s))
    //"lost_password_id" #> SHtml.onSubmitUnit(removeLostPasswordScreen)
  }


}




