package code.snippet

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import _root_.net.liftweb.http._
import _root_.net.liftweb.util._
import Helpers._
import _root_.scala.xml._
import js.JsCmd
import net.liftweb.mapper.By
import java.util.UUID


import code.model._
import net.liftweb.common.Full
import org.slf4j.LoggerFactory

class LoginForm extends StatefulSnippet {
  def logger = LoggerFactory.getLogger("LoginForm")

  //For LoginForm without AJAX
  private var (username,password) = ("","")

  private val whence = S.referer openOr "/"

  def dispatch = {case "render" => render}
  def render =
  "name=username" #> SHtml.text(username, username = _, "id" -> "username") &
  "name=password" #> SHtml.text(password, password = _, "id" -> "password") &
  "type=submit" #> SHtml.onSubmitUnit(process)

  private def process() = {
                    logger.info("Processing login request..")
    S.error("username_error", "Invalid username")
    S.error("password_error", "Invalid Password")
  }

}

