package bootstrap.liftweb

import net.liftweb._
import actor.LiftActor
import http._
import http.RewriteResponse._
import provider.servlet.containers.Jetty7AsyncProvider
import util._
import Helpers._
import common._
import sitemap._
import Loc._
import mapper._
import java.sql.{Connection, DriverManager}
import javax.mail._
import org.slf4j.LoggerFactory
import code.lib.LoginStuff
import code.model._
import code.snippet.FileVar
import code.snippet.{AjaxLoginForm, RuntimeStats}
import xml.Node


/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def logger = LoggerFactory.getLogger("Boot")
		  
  logger.info("Starting boot process for application...")

  def boot {
    logger.info("Initializing mongodb server..")

    MongoDBConfig.init

    logger.info("Initialized mongodb server.")


    object DBVendor extends ConnectionManager {
      def newConnection(name: ConnectionIdentifier): Box[Connection] = {
        try {
          Class.forName("com.mysql.jdbc.Driver")
          val dm = DriverManager.getConnection("jdbc:mysql://localhost:3306/test?user=root&password=guessme321")
          Full(dm)
        } catch {
          case e: Exception =>
            e.printStackTrace;
            logger.error(e.getMessage);
            Empty
        }
      }

      def releaseConnection(conn: Connection) {
        conn.close
      }
    }

    if (!DB.jndiJdbcConnAvailable_?) {
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    }
    // where to search snippet
    LiftRules.addToPackages("code")

    logger.info("Database connection has established.")

    // Build SiteMap
    //val allMenus = Menu(Loc("home", "index" :: Nil, "HOME")) :: Menu(Loc("about", Link(List("static"), true, "/static/index"), "ABOUT US")) :: Menu(Loc("login", Link(List("login"), true, "/login/index"),"")) :: Menu(Loc("register", "register" :: Nil, "REGISTER")) :: Nil
    // Menu entries for the User management stuff
    //User.sitemap :_*
    //def mySiteMap() = SiteMap(allMenus: _*)
    //LiftRules.setSiteMapFunc(mySiteMap)
    val entries = Menu(Loc("Home", List("index"), "Home")) ::
      Menu(Loc("Dashboard", Link(List("dashboard"), true, "/dashboard"),
        "My Home")) ::
      User.sitemap
    //LiftRules.setSiteMap(SiteMap(entries:_*))

    LiftRules.setSiteMapFunc(MenuInfo.sitemap)

    logger.info("Schemas and menu are generated successfully")

    // Uncomment the lines below to see how
    // a Lift app looks when it's stateless
    /*
    LiftRules.statelessTest.prepend {
      case _ => true
    }*/

    //LiftRules.snippetDispatch.append(Map("runtime_stats" -> RuntimeStats))

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    LiftRules.ajaxRetryCount = Full(1)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))


    //LiftRules.snippetDispatch.append {
    //    case "HelloWorld" => com.foo.logic.HelloWorld
    //}

/*
Templates(List("path", "to", "template")) match {
  case Full(ns) =>
    Mailer.sendMail(From(emailAddress),
                Subject(subject),
                To(toAddress.trim),
                XHTMLMailBodyType(ns))
  case empty =>  // note the lowercase: we're capturing it because it could be a Failure
    error("Not sending email because template could not be read: " + empty) 
*/
    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    //LiftRules.dispatch.append(User.formLogin)
	LiftRules.dispatch.append(ExcelServe)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))

    // Make a transaction span the whole HTTP request
    S.addAround(DB.buildLoanWrapper)

    LiftRules.addSyncProvider(Jetty7AsyncProvider)

    //LiftSession.onBeginServicing = RequestLogger.beginServicing _ :: LiftSession.onBeginServicing
    //LiftSession.onEndServicing = RequestLogger.endServicing _ :: LiftSession.onEndServicing

    // Dump information about session every 10 minutes
    //SessionMaster.sessionWatchers = SessionInfoDumper :: SessionMaster.sessionWatchers


    LiftRules.dispatch.prepend(NamedPF("Login Validation") {
      case Req("login" :: page, "", _)
        if !LoginStuff.is && page.head != "validate" =>
        () => Full(RedirectResponse("/login/validate"))
    })


    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath(List("user", "lostpassword", email), _, _, _), _, _) => RewriteResponse("lostpassword" :: Nil, Map("email" -> email))
      case RewriteRequest(ParsePath(List("user", "joinus"), _, _, _), _, _) => RewriteResponse("joinus" :: Nil)
      case RewriteRequest(ParsePath(List("setup", "index"), _, _, _), _, _) => RewriteResponse("setup" :: Nil)
    }

    ResourceServer.allow {
      case "ui" :: _ => true
      //case "ui" :: _ => true
    }
    LiftRules.uriNotFound.prepend {
      case _ =>
        // If we don't have a session, we can't render a template it seems
        // http://groups.google.com/group/liftweb/browse_thread/thread/512689d5d...
        S.session match {
          case Full(_) => NotFoundAsTemplate(ParsePath("404" :: Nil, "html", false, false))
          case _ =>
            // Basically send an empty response back, kinda crappy
            // This happens for urls like /images/no-such-image.jpg that are rewritten to be served out of the classpath
            val response = XhtmlResponse(<html/>, Full(DocType.xhtmlStrict), List("Content-Type" -> "application/xhtml+xml;charset=utf-8"), Nil, 404, S.ieMode)
            NotFoundAsResponse(response)
        }
    }


    import code.utils._

    Constants.EMAIL_TYPE match {
      case "localhost" =>
        Mail.configureLocalhostMailer
      case "remote" =>
        Mail.configureRemoteMailer("smtp.gmail.com", "link2life.is.beautiful", "guessme321")
        logger.trace("Email account used for gmail SMTP is link2life.is.beautiful")
      case _ =>
        logger.error("No valid type found.")
    }


    configureSettings

  }


  def configureSettings {
    import code.model._
    Setting.load()
  }


}

/*
object RequestLogger extends Loggable {

  object startTime extends RequestVar(0L)

  def beginServicing(session: LiftSession, req: Req) {
    startTime(millis)
  }

  def endServicing(session: LiftSession, req: Req,
                   response: Box[LiftResponse]) {
    val delta = millis - startTime.is
    logger.info("At " + (timeNow) + " Serviced " + req.uri + " in " + (delta) + "ms " + (
      response.map(r => " Headers: " + r.toResponse.headers) openOr ""
      ))
  }
}



object SessionInfoDumper extends LiftActor with Loggable {
  private var lastTime = millis

  private def cyclePeriod = 10 minute

  protected def messageHandler = {
    case SessionWatcherInfo(sessions) =>
      if ((millis - cyclePeriod) > lastTime) {
        lastTime = millis
        val rt = Runtime.getRuntime
        rt.gc

        RuntimeStats.lastUpdate = timeNow
        RuntimeStats.totalMem = rt.totalMemory
        RuntimeStats.freeMem = rt.freeMemory
        RuntimeStats.sessions = sessions.size

        val percent = (RuntimeStats.freeMem * 100L) / RuntimeStats.totalMem

        // get more aggressive about purging if we're
        // at less than 35% free memory
        if (percent < 35L) {
          SessionChecker.killWhen /= 2L
          if (SessionChecker.killWhen < 5000L)
            SessionChecker.killWhen = 5000L
          SessionChecker.killCnt *= 2
        } else {
          SessionChecker.killWhen *= 2L
          if (SessionChecker.killWhen >
            SessionChecker.defaultKillWhen)
            SessionChecker.killWhen = SessionChecker.defaultKillWhen
          val newKillCnt = SessionChecker.killCnt / 2
          if (newKillCnt > 0) SessionChecker.killCnt = newKillCnt
        }

        val dateStr: String = timeNow.toString
        logger.info("[MEMDEBUG] At " + dateStr + " Number of open sessions: " + sessions.size)
        logger.info("[MEMDEBUG] Free Memory: " + pretty(RuntimeStats.freeMem))
        logger.info("[MEMDEBUG] Total Memory: " + pretty(RuntimeStats.totalMem))
        logger.info("[MEMDEBUG] Kill Interval: " + (SessionChecker.killWhen / 1000L))
        logger.info("[MEMDEBUG] Kill Count: " + (SessionChecker.killCnt))
      }
  }


  private def pretty(in: Long): String =
    if (in > 1000L) pretty(in / 1000L) + "," + (in % 1000L)
    else in.toString

}

object SessionChecker extends Function2[Map[String, SessionInfo],
  SessionInfo => Unit, Unit] with Logger {
  def defaultKillWhen = 180000L

  // how long do we wait to kill single browsers
  @volatile var killWhen = defaultKillWhen

  @volatile var killCnt = 1

  def apply(sessions: Map[String, SessionInfo],
            destroyer: SessionInfo => Unit): Unit = {
    val cutoff = millis - 180000L

    sessions.foreach {
      case (name, si@SessionInfo(session, agent, _, cnt, lastAccess)) =>
        if (cnt <= killCnt && lastAccess < cutoff) {
          info("Purging " + agent)
          destroyer(si)
        }
    }
  }
}
*/


object MenuInfo {

  import Loc._

  def sitemap() = SiteMap(
    Menu("Home") / "index",
    Menu("Login") / "login",
    Menu("Dashboard") / "dashboard",
    Menu("LostPassword") / "lostpassword",
    Menu("JoinUs") / "joinus",
    Menu("LinkedInConnect") / "linkedin",
    Menu("Social") / "social",
    Menu("Emails") / "emails",
    Menu("Tracking") / "tracking",
    Menu("Setup") / "setup",
    Menu("Export") / "export",
	Menu("Audience") / "audience",
	Menu("Edit Pet") / "audienceedit",
    //Menu(Loc("Login", Link(List("login")), "/login/index")),
    Menu(Loc("src", ExtLink("https://github.com/lift/lift/tree/master/examples/example"), "My Account"))
  )
}

import rest._

object ExcelServe extends RestHelper {
  serve {
    case "export" :: _ Get _ =>
      println("Here")
      for {
        stream <- Box !! this.getClass.getResourceAsStream("/tmp/LinkedInUsers-2011-August-25-15h57m.xls")
      } yield InMemoryResponse(Helpers.readWholeStream(stream),
                               "Cache-Control" -> "maxage=1" ::
                               "Pragma" -> "public" ::
                                "Content-Type" ->
                               "application/vnd.ms-excel" :: Nil, Nil, 200)

  }
}

