package code.snippet {

import code.model._

import _root_.net.liftweb._
import http._
import mapper._
import S._
import SHtml._

import common._
import util._
import Helpers._

import _root_.scala.xml.{NodeSeq, Text, Group}
import _root_.java.util.Locale

class Misc {
  private object selectedUser extends RequestVar[Box[User]](Empty)
}

object definedLocale extends SessionVar[Box[Locale]](Empty)


}
