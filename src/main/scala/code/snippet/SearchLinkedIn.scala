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

object SearchLinkedIn {
  def logger = LoggerFactory.getLogger("SearchLinkedIn")


  val degreeList = Map("" -> "All", "1" -> "One", "2" -> "Two", "4" -> "In Group", "3" -> "Not In Network")
  val companyPeopleSearchList = List("People", "Company")
    var (keywords, first_name, last_name, degree, location, industry, company, role, comp_size, interests, spl, is_company_search) =
      ("", "", "", "", "", "", "", "", "", "", "", "")

      def render(html:NodeSeq):NodeSeq = { 
	logger.info("Rendering SearchLinkedIn........")
    bind("sff", html,
      "keywords" -> SHtml.text(keywords, keywords = _),
      "first_name" -> SHtml.text(first_name, first_name = _
      ),
      "last_name" -> SHtml.text(last_name, last_name = _),
      "degree" -> SHtml.select(degreeList.toSeq, Full(degree), degree = _),
      "location" -> SHtml.text(location, location = _),
      "company" -> SHtml.text(company, company = _),
      "role" -> SHtml.text(role, role = _),
      "comp_size" -> SHtml.text(comp_size,comp_size = _),
      "interests" -> SHtml.text(interests,interests =  _),
      "spl" -> SHtml.text(spl,spl = _),
      "is_company_search" -> SHtml.select(companyPeopleSearchList.map(a => (a, a)), Full(is_company_search),is_company_search = _),
	 "business" -> SHtml.select(LinkedInConstants.LINKEDIN_INDUSTRY_TABLE.toSeq.sortBy(_._2), Full(industry), industry = _),
      "search" -> SHtml.submit("Search", processSearch))

      }
    def processSearch() {
      logger.info("This is processing of search of LinkedIn...")
      "#search_messages" #> Text("Results are shown below..")

    }

  def searchFilterForm(html: NodeSeq): NodeSeq = {
    var peopleSearchEntity = new PeopleSearchEntity()


    bind("sff", html,
      "keywords" -> SHtml.text(keywords, keywords = _),
      "first_name" -> SHtml.text(first_name, first_name = _
      ),
      "last_name" -> SHtml.text(last_name, last_name = _),
      "degree" -> SHtml.select(degreeList.toSeq, Full(degree), degree = _),
      "location" -> SHtml.text(location, location = _),
      "company" -> SHtml.text(company, company = _),
      "role" -> SHtml.text(role, role = _),
      "comp_size" -> SHtml.text(comp_size,comp_size = _),
      "interests" -> SHtml.text(interests,interests =  _),
      "spl" -> SHtml.text(spl,spl = _),
      "is_company_search" -> SHtml.select(companyPeopleSearchList.map(a => (a, a)), Full(is_company_search),is_company_search = _),
	 "business" -> SHtml.select(LinkedInConstants.LINKEDIN_INDUSTRY_TABLE.toSeq.sortBy(_._2), Full(industry), industry = _),
      "search" -> SHtml.submit("Search", processSearch, "id" -> "search"))

  }
}
