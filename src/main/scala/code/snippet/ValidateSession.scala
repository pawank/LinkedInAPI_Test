package code.snippet

import xml.NodeSeq
import code.lib.LoginStuff
import net.liftweb.http.{SHtml, S}
import net.liftweb.http.js.JsCmds.RedirectTo

class ValidateSession {

   /**
    * This method is invoked by the &lt;lift:Count /&gt; tag
    */
    def render(in: NodeSeq): NodeSeq =
      SHtml.ajaxButton("Validate",
                       () => {
                         LoginStuff(true)
                         S.notice("Your session is validated")
                         RedirectTo("/login/index")
                       })
}
