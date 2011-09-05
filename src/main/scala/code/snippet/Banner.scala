package code.snippet
import scala.xml.NodeSeq
import net.liftweb.util._
import code.model.User
import net.liftweb.common.Full
import net.liftweb.http._

class Banner {
   def render(in: NodeSeq): NodeSeq =           {
     /*
     User.currentUser match {
     case Full(user) => {
          <p>Thanks again for logging in.</p>
     }
     case _ => <lift:embed what="/templates-hidden/welcome"/>
     */

      println("CONTEXT PATH = " + S.uri + " LIFT PATH=" + LiftRules.context.path)
     if (S.uri == "/")
       in
     else
       <div id="tmp"></div>
   }

}