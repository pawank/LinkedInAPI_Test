package code.snippet

import net.liftweb.http.{S, LiftScreen}
import util._

object LoginScreen extends LiftScreen {
val username = field("Username", "")
  val pass = password("Password", "", valMinLen(6, "Password too short"))

def finish() {
S.notice("Name: "+username)
S.notice("Age: "+pass)
}

}
