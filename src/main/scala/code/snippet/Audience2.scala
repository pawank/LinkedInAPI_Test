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

object Audience2 {
//object Audience extends PaginatorSnippet[UserSocialProfile] {
//class Audience extends StatefulSortedPaginatorSnippet[MetaRecord, String] {
  def logger = LoggerFactory.getLogger("Audience")
  //def logger = Logger(classOf[Audience])

  val msgList = List("CONNECT", "MESSAGE", "MORE INFO", "DICONNECT", "ENGAGE")
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
    
  var users = {
    //UserSocialProfile.findAll(QueryBuilder.start().get(), Limit(itemsPerPage), Skip(curPage * itemsPerPage))
    import com.mongodb._
      //var q = UserSocialProfile.where(_.is_active eqs true)

    //if ((is_company_search == "") || (is_company_search == "P")) {
      var q = UserSocialProfile.where(_.is_active eqs true)
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
      //logger.debug("Q for search and filter users and companies on LinkedIn:" + q.toString())
      //q.limit(itemsPerPage).skip(curPage * itemsPerPage).fetch()
      q.fetch()
    //} 
    //}
      //q.limit(itemsPerPage).skip(curPage * itemsPerPage).fetch()
  }

  var companies = { 
  
      var q = CompanyProfile.where(_.is_active eqs true)
      if (company != "") {
        q = q and (_.company_name regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(company)))
        q = q and (_.universal_name regexWarningNotIndexed Pattern.compile(GeneralUtils.getCaseInsensitiveRegex(company)))

      }

      logger.debug("Q for search and filter companies on LinkedIn:" + q.toString())
      //q.limit(itemsPerPage).skip(curPage * itemsPerPage).fetch()
      q.fetch()
  
  }

  //override def headers = List("picture", "nameuser", "conns","degree","con_non","loc","headline","invitation_sent").map(s => (s, s)) 

  def testclick: NodeSeq = {
    logger.debug("clicked test")
    <p>test</p>
  }
  def show(html: NodeSeq): NodeSeq = {
    //logger.debug("Rendering Audience snippet....")
    var name = ""
    var conns = "0"
    var ints = ""
    var check = ""

    if ((is_company_search == "") || (is_company_search == "P")) { 
    users.flatMap(u =>
      bind("aud", html,
        "picture" -> SHtml.link("a", () => testclick, {
          for (l <- u.extended_profile.is) {
            l match {
              case ExtendedProfile(a, b) =>
                logger.debug("Summary using extended profile:" + a)
              case _ =>
                logger.debug("Not matched ExtendedProfile.")
            }
          }
          var url = u.picture_url.value;
          //logger.debug("Picture url is " + url);
          if (url.length == 0)
            url = "/classpath/ui/assets/images/ajax-loader.gif";
          //NOTE: Remove me after testing
          url = "/classpath/ui/assets/images/pawan_linkedin.jpg";
          <img src={ url }/>
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
        "interests" -> (u.interests.value),
        "invitation_sent" -> ({
          val v = true
          var url = "/classpath/ui/assets/images/invitation_not_sent.jpg";
          if (v == true)
            url = "/classpath/ui/assets/images/invitation_sent.jpg";
          <img src={ url } width="30px" height="30px"/>
        }),
        "msgtype" ->
          manageSelect _ //SHtml.select(msgList.map(v => (v.toString, v.toString)), Empty, testselect _)
          ))
    
    }
    
    else { 
    companies.flatMap(u =>
      bind("aud", html,
        "picture" -> SHtml.link("a", () => testclick, {
          var url = u.website_url.value;
          //logger.debug("Picture url is " + url);
          if (url.length == 0)
            url = "/classpath/ui/assets/images/ajax-loader.gif";
          //NOTE: Remove me after testing
          url = "/classpath/ui/assets/images/pawan_linkedin.jpg";
          <img src={ url }/>
        }),
        "nameuser" -> (u.company_name.value),
        "conns" -> (u.no_followers.value.toString),
        "degree" -> (u.emp_count_range.value.toString),
        "con_non" -> ({
          val v = u.social_logo_url.value;
          var url = "/classpath/ui/assets/images/not_connected.jpg";
          if (v == true)
            url = "/classpath/ui/assets/images/connected.jpg";
          <img src={ url } width="30px" height="30px"/>
        }),
        "loc" -> (u.location.value),
        "headline" -> (u.specialities.value.substring(0, u.specialities.value.length/3)),
        "invitation_sent" -> ({
          val v = true
          var url = "/classpath/ui/assets/images/invitation_not_sent.jpg";
          if (v == true)
            url = "/classpath/ui/assets/images/invitation_sent.jpg";
          <img src={ url } width="30px" height="30px"/>
        }),
        "msgtype" ->
          manageSelect _ //SHtml.select(msgList.map(v => (v.toString, v.toString)), Empty, testselect _)
          ))

    }

  }

  def manageSelect(html: NodeSeq) = {
    val msgtxt = "messages"
    SHtml.ajaxSelect(msgList.map(v => (v.toString, v.toString)), Full(""),
      s => DisplayMessage(msgtxt, bind("m", html, "v" -> Text("Action " + s + " executed")), 5 seconds, 2 seconds))

  }

  def userSocialProfile(html: NodeSeq): NodeSeq = {
    //logger.debug("Rendering user social profile summary information....")
    users.map(a => total = (a.no_connections.value.toInt + total))

    bind("c", html,
      "total" -> (total.toString),
      "potential" -> (potential.toString),
      "invsent" -> (invsent.toString))

  }


  def searchFilterForm(html: NodeSeq): NodeSeq = {
    var peopleSearchEntity = new PeopleSearchEntity()

    def processSearch() {
      println("This is processing of search of LinkedIn...")
      //processSearchFilterForm(html, peopleSearchEntity)

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

  def f(l: List[String]): Unit = {
  }
}
