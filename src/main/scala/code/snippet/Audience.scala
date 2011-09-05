package code.snippet

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util.Helpers._
import net.liftweb.sitemap._
import scala.xml._
import net.liftweb.http.S._
import net.liftweb.http.RequestVar
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import code.model.UserSocialProfile
import net.liftweb.mongodb.{ Skip, Limit }
import net.liftweb.http.S._
import net.liftweb.mapper.view._
import com.mongodb._
import SHtml._
import S._
import code.model._
import js._
import JsCmds._
import net.liftweb.common.Logger
import org.slf4j.LoggerFactory
import net.liftweb.http.js.jquery.JqJsCmds.DisplayMessage
import net.liftweb.http.PaginatorSnippet
import code.linkedin._
import com.foursquare.rogue.Rogue._
import java.util.regex.Pattern
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.record._
import org.bson.types._
import net.liftweb.json.JsonDSL._
import code.utils._
import code.export._

object FileVar extends RequestVar[Box[String]](Empty)
//object Audience extends PaginatorSnippet[UserSocialProfile] {
object Audience {

  def logger = LoggerFactory.getLogger("Audience")
  //def logger = Logger(classOf[Audience])

  val msgList = List("----------","CONNECT", "MESSAGE")
  var total = 0
  var potential = 0
  var invsent = 0
  /*
  override def count = {
    if ((is_company_search == "") || (is_company_search == "P")) {
      UserSocialProfile.count
    } else {
      CompanyProfile.count
    }
  }
  override def itemsPerPage = 5
  override def prevXml: NodeSeq = Text(?("<"))
  override def nextXml: NodeSeq = Text(?(">"))
  override def firstXml: NodeSeq = Text(?("Start <<"))
  override def lastXml: NodeSeq = Text(?(">> End"))
  override def currentXml: NodeSeq = Text("Displaying records " + (first + 1) + "-" + (first + itemsPerPage min count) + " of " + count)
  */
  val degreeList = Map("" -> "All", "1" -> "One", "2" -> "Two", "4" -> "In Group", "3" -> "Not In Network")
  val companyPeopleSearchList = Map("P" -> "People", "C" -> "Company")
  var (keywords, first_name, last_name, degree, location, industry, company, role, comp_size, interests, spl, is_company_search) =
    ("", "", "", "", "", "", "", "", "", "", "", "")

    val itemsPerPage = 100
    val curPage = 0

  var page = getUsers

  def testclick: NodeSeq = {
    logger.debug("clicked test")
    <p>test</p>
  }
  
  def getUsers():List[UserSocialProfile] = { 

    //UserSocialProfile.findAll(QueryBuilder.start().get(), Limit(itemsPerPage), Skip(curPage * itemsPerPage))
    import com.mongodb._
    var q = UserSocialProfile.where(_.is_active eqs true)

    if ((is_company_search == "") || (is_company_search == "P")) {
      //var q = UserSocialProfile.where(_.is_active eqs true)
      if (first_name != "") {
        val p = "(?i)[a-zA-Z]*" + first_name + "[a-zA-Z]*"

        q = q and (_.first_name regexWarningNotIndexed Pattern.compile(p))
      }
      if (last_name != "") {
        q = q and (_.last_name regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(last_name)))

      }
      if (degree != "")
        q = q and (_.graph_distance eqs degree.toInt)

      if (keywords != "") {
        for (i <- keywords.split(" ")) {
          q = q and (_.headline regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(i)))
          q = q and (_.summary regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(i)))
        }
      }
      if (location != "") {
        q = q and (_.location regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(location)))

      }
      if (interests != "") {
        q = q and (_.interests regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(interests)))

      }
      if (spl != "") {
        q = q and (_.specialities regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(spl)))

      }
      if (company != "") {
        q = q and (_.company_with_position regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(company)))

      }

      if (industry != "") {
        LinkedInConstants.LINKEDIN_INDUSTRY_TABLE.get(industry) match {
          case Some(x) =>
            q = q and (_.industry eqs x)
          case _ =>
        }
      }
      logger.debug("Q for search and filter users and companies on LinkedIn:" + q.toString())
      //q.limit(itemsPerPage).skip(curPage * itemsPerPage).fetch()
    }
    q.fetch()

  }
  val l = "LinkedIn-Users-List.xls"
  val downloadLink = "http://content.efoundry.in/reports/" + l

  def download(html: NodeSeq): NodeSeq = {
    import com.github.philcali.scalendar._    
    val timenow = Scalendar.now

    bind("dow", html,
	 "excel" ->  <a href={downloadLink}>Download Excel</a>
       )
  }

  def show(html: NodeSeq): NodeSeq = {
    //logger.debug("Rendering Audience snippet....")
    var name = ""
    var conns = "0"
    var ints = ""
    var check = ""
    import com.github.philcali.scalendar._    
    val timenow = Scalendar.now
    //val l = "LinkedInUsers-" + timenow.year.value + "-" + timenow.month + "-" + timenow.day.value + "-" + timenow.hour.value + "h" + timenow.minute.value + "m.xls"

    Excel.getUserDetailExcel("LinkedIn Users",page,l)


    page.flatMap(u =>

      bind("aud", html,
        "picture" -> SHtml.link("a", () => testclick, {
	  /*
          for (l <- u.extended_profile.is) {
            l match {
              case ExtendedProfile(a, b) =>
                logger.debug("Summary using extended profile:" + a)
              case _ =>
                logger.debug("Not matched ExtendedProfile.")
            }
          }*/
          var url = u.picture_url.value;
          //logger.debug("Picture url is " + url);
          if (url.length == 0)
            url = "/classpath/ui/assets/images/not_available.jpg";
          //NOTE: Remove me after testing
          //url = "/classpath/ui/assets/images/pawan_linkedin.jpg";
          <img src={ url } width="80px" height="80px"/>
        }),
        "nameuser" -> (u.first_name.value + " " + u.last_name.value),
        "conns" -> (u.no_connections.value.toString),
        "degree" -> (u.graph_distance.value.toString),
        "con_non" -> ({
          val v = u.is_connected.value;
          var url = "/classpath/ui/assets/images/not_connected.jpg";
          if (v == true)
            url = "/classpath/ui/assets/images/connected.jpg";
          <img src={ url } width="30px" height="30px"/>
        }),
        "loc" -> (u.location.value),
        "headline" -> (u.headline.value),
        "invitation_sent" -> ({
          val v = true
          var url = "/classpath/ui/assets/images/invitation_not_sent.jpg";
          if (v == true)
            url = "/classpath/ui/assets/images/invitation_sent.jpg";
          <img src={ url } width="30px" height="30px"/>
        }),
        "msgtype" ->
          manageSelect(html, u)
          ))

  }

  def manageSelect(html: NodeSeq, u:UserSocialProfile) = {
    val msgtxt = "messages"
    SHtml.ajaxSelect(msgList.map(v => (u.social_user_id.is + ":" + v.toString, v.toString)), Full(""),
      s => DisplayMessage(msgtxt, bind("m", html, "v" -> { 
	val tokens = s.split(":")
	var msg = ""
	logger.debug("Connect tokens:" + tokens.toString())
	if (tokens(1).equals("CONNECT")) { 
	  var q = UserSocialProfile.where(_.is_active eqs true)
	  q = q and (_.social_user_id eqs tokens(0))
	  q.get() match { 
	    case Some(user) =>
		var firstname = ""
	    S.get("LINKEDIN_CURRENT_USER_FIRST_NAME") match { 
	      case Full(fn) => firstname = fn
	      case _ =>
	    }
	    var lastname = ""
	      S.get("LINKEDIN_CURRENT_USER_LAST_NAME") match { 
	      case Full(fn) => lastname = fn
	      case _ =>
	      
	      }
	    
	    var id = ""
	    S.get("LINKEDIN_CURRENT_USER_ID") match { 
	      case Full(fn) => id = fn
	      case _ =>
	    
	    }
	    logger.debug("LinkedIn current user info, id=" + id + " and last name=" + lastname)
	      val see = SocialEmailEntity(user.first_name.is, user.last_name.is,"",firstname,lastname,"","Test","Test message",null,null,user.social_user_id.is, id)
	    
	    val cred  = LinkedInXmlCredentialEntity(user.api_std_profile_http_name.is, user.api_std_profile_http_value.is)

	    val (status, payload) = LinkedInConnect.prepareInvitationEmail("",see,cred)

	    if (status) {
	      logger.debug("Payload for connect:" + payload)
	      val api_url = "http://api.linkedin.com/v1/people/~/mailbox"

	      val (token, needsRedirect) = LinkedInConnect.authenticate("", "")
	      if (needsRedirect) {
		val url = LinkedInConnect.getLinkedInAuthorizationUrl(token)
		logger.debug("Action is to redirect to LinekdIn oauth link - " + url)
		//JsCmds.RedirectTo(url)
		S.redirectTo(url)
		logger.debug("This should not appear in log..")
	      } else {
		logger.debug("Session of LinkedIn found and processing the request...")
		val (st,data) = LinkedInConnect.sendInvitationEmail(token,api_url,payload)
		logger.debug("Response of invitation:" + data)
		msg = data
	      }
	      if (token != null)
		S.set("linkedin_session_valid", "1")
	      else { 
		logger.warn("Token value is empty.")
	      }
	    }else { 
	      logger.debug("Error in preparing invitation email")
	    }
	      
	    case _ =>
	  }
	  
	  
	}
	Text("Message from server:" + msg)
      }), 5 seconds, 2 seconds))

  }

  def userSocialProfile(html: NodeSeq): NodeSeq = {
    //logger.debug("Rendering user social profile summary information....")
    //page.map(a => total = (a.no_connections.value.toInt + total))

    bind("c", html,
      "total" -> (total.toString),
      "potential" -> (potential.toString),
      "invsent" -> (invsent.toString))

  }

  def connectAndFetchDataFromLinkedIn(api_url: String): NodeSeq = {
    logger.debug("Connecting to LinkedIn using API URL:" + api_url)
    var data = ""

    val (token, needsRedirect) = LinkedInConnect.authenticate("", "")
    if (needsRedirect) {
      val url = LinkedInConnect.getLinkedInAuthorizationUrl(token)
      logger.debug("Action is to redirect to LinekdIn oauth link - " + url)
      //JsCmds.RedirectTo(url)
      S.redirectTo(url)
      logger.debug("This should not appear in log..")
    } else {
      logger.debug("Session of LinkedIn found and processing the request...")
      data = LinkedInConnect.processRequest(token, api_url)
    }
    if (token != null)
      S.set("linkedin_session_valid", "1")
    else { 
      logger.warn("Token value is empty.")
    }

    //logger.info("Sending response XML back to calling prorgam as [" + data + "]")
    <data>
      { data }
    </data>
  }

  def searchFilterForm(html: NodeSeq): NodeSeq = {


    def processSearch() {
      logger.info("This is processing of search of LinkedIn people search...")
      var peopleSearchEntity = new PeopleSearchEntity()
      peopleSearchEntity.keywords = keywords
      peopleSearchEntity.first_name = first_name
      peopleSearchEntity.last_name = last_name
      peopleSearchEntity.degree = degree
      peopleSearchEntity.location = location
      peopleSearchEntity.role = role
      peopleSearchEntity.comp_size = comp_size
      peopleSearchEntity.interests = interests
      peopleSearchEntity.specialities = spl
      peopleSearchEntity.company_name = company
      peopleSearchEntity.industry = industry
      if (is_company_search.equalsIgnoreCase("P"))
        peopleSearchEntity.is_company_search = false
      else
        peopleSearchEntity.is_company_search = true

      var total = 200
      var count = 200
      var start = 0
      

      while((start + count) <= total) { 
	val (status, query) = LinkedInConnect.preparePeopleSearchQuery(peopleSearchEntity, start, count)
	logger.debug("Q preparation done..")
	if (status) {
	  logger.debug("Q for LinkedIn:" + query)
        val node = connectAndFetchDataFromLinkedIn(query)
	  if (!(node \\ "people").isEmpty) { 
	    total = (node \\ "people" \ "@total").text.toInt
	    count = (node \\ "people" \ "@count").text.toInt
	    start = (node \\ "people" \ "@start").text.toInt
	    logger.info("total, count and start are:" + total + "," + count + "," + start)
            val (st, msg) = LinkedInConnect.savePeopleSearch("pawan.kumar@gmail.com", node.toString())
            logger.debug("Status of people search is:" + st + " and msg:" + msg)
	  }
	  else { 
	    logger.error("Node XML returned from LinkedIn is invalid.")
	  }
	  
	}else { 
	  logger.debug("Query preparation has error...")
	}
	start = count + 1
      }
      logger.debug("Saving of all records in mongodb is done..Now, get list of all matching users..")
      page = getUsers()      
      logger.debug("Loaded all matching users")
    }

    bind("sff", html,
      "keywords" -> SHtml.text(keywords, keywords = _),
      "first_name" -> SHtml.text(first_name, first_name = _),
      "last_name" -> SHtml.text(last_name, last_name = _),
      "degree" -> SHtml.select(degreeList.toSeq, Full(degree), degree = _),
      "location" -> SHtml.text(location, location = _),
      "company" -> SHtml.text(company, company = _),
      "role" -> SHtml.text(role, role = _),
      "comp_size" -> SHtml.text(comp_size, comp_size = _),
      "interests" -> SHtml.text(interests, interests = _),
      "spl" -> SHtml.text(spl, spl = _),
      "is_company_search" -> SHtml.select(companyPeopleSearchList.toSeq, Full(is_company_search), is_company_search = _),
      "business" -> SHtml.select(LinkedInConstants.LINKEDIN_INDUSTRY_TABLE.toSeq.sortBy(_._2), Full(industry), industry = _),
      "search" -> SHtml.submit("Search", processSearch, "id" -> "search"))

  }

}
