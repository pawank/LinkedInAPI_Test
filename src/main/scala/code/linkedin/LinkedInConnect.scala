package code.linkedin

import org.slf4j.LoggerFactory
import net.liftweb.http.{ RequestVar, js, SHtml, S }
import org.scribe.oauth.OAuthService
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.LinkedInApi
import org.scribe.model._
import java.lang.Boolean
import java.io.IOException
import xml.{ Elem, XML, NodeSeq, Text }
import net.liftweb.json._
import com.github.philcali.scalendar.Scalendar
import net.liftweb.mongodb.{ MongoDB, DefaultMongoIdentifier }
import code.model._
//import com.mongodb._

object LinkedInConnect {
  def logger = LoggerFactory.getLogger("LinkedInConnect")
  val simulateXmlLoadingForTest = false

  // Values stored in the session
  val KEY_REQUEST_TOKEN = "requestToken"
  val KEY_REQUEST_TOKEN_SECRET = "requestTokenSecret"
  val KEY_ACCESS_TOKEN = "accessToken"
  val KEY_ACCESS_TOKEN_SECRET = "accessTokenSecret"
  val showResponseData = true
  
  //scribe
  val service: OAuthService = {
    new ServiceBuilder()
      .provider(classOf[LinkedInApi])
      .apiKey("XXXXXXXXXXXXXXX")
      .apiSecret("XXXXXXXXXXXXXXXXX")
      .callback("http://localhost:8080/social")
      .build()

  }

  def getLinkedInAuthorizationUrl(requestToken: Token): String = {
    /*
            // Obtain the Request Token
            logger.debug("Fetching the Request Token...");
            val requestToken:Token = service.getRequestToken();
            logger.debug("Got the Request Token!");
            */
    logger.debug("Now go and authorize application here");
    val url = service.getAuthorizationUrl(requestToken);

    S.set(KEY_REQUEST_TOKEN, requestToken.getToken())
    S.set(KEY_REQUEST_TOKEN_SECRET, requestToken.getSecret())
    logger.info("Redirecting to " + url + "\n\n")
    //S.redirectTo(url)
    url
  }

  def processRequest(accessToken: Token, apiUrl: String): String = {
    //S.skipXmlHeader = true
    //Return empty data if API URL is invalid or not set
    if (apiUrl != "") {
      logger.debug("=== Fetching data from LinkedIn's URL [" + apiUrl + "]...");
      val request: OAuthRequest = new OAuthRequest(Verb.GET, apiUrl);
      service.signRequest(accessToken, request);

      logger.debug("Signing in to LinkedIn with access token..")
      val response: Response = request.send();
      val data = response.getBody
      if (showResponseData)
	logger.debug(response.getBody());
      logger.debug("Received response for the LinkedIn request");
      data
    } else
      ""
  }

  def processOAuth(accessToken: Token): String = {
    //S.skipXmlHeader = true
    logger.debug("=== LinkedIn's OAuth Workflow ===");
    logger.debug("Access token for LinkedIn looks like - (" + accessToken + ")");
    // Now let's go and ask for a protected resource!
    logger.debug("Now we're going to access a protected resource...");
    val PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~/connections:(id,last-name)"
    val request: OAuthRequest = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
    service.signRequest(accessToken, request);
    logger.debug("Signing in to LinkedIn with access token..")
    val response: Response = request.send();
    val data = response.getBody
    if (showResponseData)
      logger.debug(response.getBody());
    logger.debug("Received response for the LinkedIn request");
    data
  }

  //the request token string and secret make up the request token and are stored in the session
  private def rebuildRequestToken(): Token = {
    val token = S.get(KEY_REQUEST_TOKEN).openOr("")
    val secret = S.get(KEY_REQUEST_TOKEN_SECRET).openOr("")
    logger.info("Rebuilding with request token        " + token)
    logger.info("Rebuilding with request token secret " + secret)
    if ((token == "") && (secret == ""))
      logger.error("Request token saved in session for LinkedIn are not valid.")

    new Token(token, secret)
  }

  def authenticate(oauth_token: String, oauth_verifier: String): (Token, Boolean) = {
    val accessTokenToken = S.get(KEY_ACCESS_TOKEN).openOr("")
    val accessTokenSecret = S.get(KEY_ACCESS_TOKEN_SECRET).openOr("")
    val needsRedirect = true

    if (accessTokenToken != "" && accessTokenSecret != "") {
      //have the access token already so get its parts out of the session and reconstruct
      logger.debug("Already logged in")
      logger.debug("session access token         :" + accessTokenToken)
      logger.debug("session access token secret  :" + accessTokenSecret)
      val accessToken = new Token(accessTokenToken, accessTokenSecret)
      (accessToken, !needsRedirect)
    } else if ((oauth_verifier != null) && (oauth_verifier != "")) {
      //got redirected from LinkedIn and the oauth_verifier is passed as a parameter
      logger.debug("Redirected from LinkedIn with oauth_verifier " + oauth_verifier)
      val verifier = new Verifier(oauth_verifier)
      val accessToken = service.getAccessToken(rebuildRequestToken(), verifier)
      cleanSession()
      saveAccessToken(accessToken);
      (accessToken, !needsRedirect)
    } else {
      logger.debug("Fresh Start for LinekdIn")
      val requestToken = service.getRequestToken()
      logger.debug("Got request token: " + requestToken.toString())
      (requestToken, needsRedirect)
    }
  }

  private def saveAccessToken(t: Token): Unit = {
    S.set(KEY_ACCESS_TOKEN, t.getToken)
    S.set(KEY_ACCESS_TOKEN_SECRET, t.getSecret)
  }

  def cleanSession() = {
    S.set(KEY_REQUEST_TOKEN, "")
    S.set(KEY_REQUEST_TOKEN_SECRET, "")
    S.set(KEY_ACCESS_TOKEN, "")
    S.set(KEY_ACCESS_TOKEN_SECRET, "")
  }


  def saveProfile(email: String, content: String): (Boolean, String) = {
    //Parse profile XML and save in database
    var data: Elem = null
    var status = true
    var message = ""
    if (simulateXmlLoadingForTest) {
      try {
        val source = scala.io.Source.fromFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_profile.xml")
        //data = source.mkString
        //data = XML.loadFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_profile.xml")
        data = XML.loadString(source.mkString.replaceAll("&", "&amp;"))
	if (showResponseData)
          logger.debug("Profile XML - " + data)
        //source.close()
      } catch {
        case e: IOException =>
          e.printStackTrace()
          message = e.getMessage
          logger.error("Error occurred in reading profile xml file:" + message)
      }
    } else {
      data = XML.loadString(content.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&quot;","'").replaceAll("&","&amp;").replaceAll("<data>","").replaceAll("</data>","").trim)
      //data = XML.loadString(content)
    }
    val (s, m) = checkXmlErrors(data)
    if (s == false)
      return (s, m)

    if ((data != null) && (s == true)) {
      var id = ""
      if (!(data \\ "id").isEmpty)
	id = (data \\ "id").text

      var usp = UserSocialProfile.createRecord

      val uspList = UserSocialProfile.findAll(com.mongodb.QueryBuilder.start("social_user_id").is(id).get)
      if (uspList.length == 0) {
        logger.debug("User with social_user_id=" + id + " do not exists. So, add user information.")

      } else {
        logger.debug("User with social_user_id=" + id + " already exists. So, update its information.")
        usp = uspList(0)
      }

      var noconn = -1
      if (!(data \\ "num-connections").isEmpty)
        noconn = (data \\ "num-connections").text.toInt

      //val usp = UserSocialProfile.createRecord
      usp.username_email.set(email)
      usp.social_user_id.set(id.toString)
      usp.connection_user_id.set("")

      usp.first_name.set((data \\ "first-name").text)
      usp.last_name.set((data \\ "last-name").text)

      S.set("LINKEDIN_CURRENT_USER_FIRST_NAME",(data \\ "first-name").text)
      S.set("LINKEDIN_CURRENT_USER_LAST_NAME",(data \\ "last-name").text)
      S.set("LINKEDIN_CURRENT_USER_ID", id.toString)

      usp.headline.set((data \\ "headline").text)
      usp.location.set((data \ "location" \ "name").text)
      usp.industry.set((data \\ "industry").text)
      usp.no_connections.set(noconn.toString().toInt)
      //Does search from a user account for another person on LinkedIn gives graph distance between the two?
      //TODO
      usp.graph_distance.set(0)
      usp.interests.set((data \\ "interests").text)
      if ((data \ "date-of-birth" \ "year").text != "") { 
      val year = (data \ "date-of-birth" \ "year").text.toInt
      val month = (data \ "date-of-birth" \ "month").text.toInt
      val day = (data \ "date-of-birth" \ "day").text.toInt

      usp.dob.set(Scalendar(year = year, month = month, day = day))

      }
      usp.main_address.set((data \\ "main-address").text)
      usp.picture_url.set((data \\ "picture-url").text)
      usp.public_profile_url.set((data \\ "public-profile-url").text)
      val phone = (data \\ "phone-number" \ "phone-number").text
      usp.phone_nos.set(phone)
      usp.phones.set(List(Phones(((data \\ "phone-number" \ "phone-number").text), (data \\ "phone-number" \ "phone-type").text)))
      usp.educations.set((List(Educations((data \\ "education" \ "id").text, (data \\ "education" \ "school-name").text, (data \\ "education" \ "start-date" \ "year").text, (data \\ "education" \ "end-date" \ "year").text))))
      usp.extended_profile.set(List(ExtendedProfile((data \\ "summary").text, (data \\ "specialties").text)))
      usp.summary.set((data \\ "summary").text)
      usp.specialities.set((data \\ "specialties").text)
      var company = ""
            for(d <- (data \\ "three-current-positions" \ "position")) { 
	      company += (d \ "title").text + "," + (d \ "company" \ "name").text + "#"
	    }

      for(d <- (data \\ "three-past-positions" \ "position")) { 
	company += (d \ "title").text + ", " + (d \ "company" \ "name").text + "#"
      }
      usp.company_with_position.set(company)
      usp.api_std_profile_http_name.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "name").text)
      usp.api_std_profile_http_value.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "value").text)
      usp.action_type.set("PROFILE")
      usp.save

      def fromDb = UserSocialProfile.find("_id", usp.id)

      if (fromDb.isDefined == false) {
        logger.error("Error in saving record with ID - " + usp.id + "\n\n")
      } else {
        logger.info("Profile XML is saved in database and has ID - " + usp.id + "\n")
      }

      //MongoDB.useCollection("stringdatedocs") { coll => }

      logger.debug("LinkedIn user profile ID to be save in db:" + id.toString)
      //val jsonData = Xml.toJson(data)
      //logger.debug("Profile XML JSON:" + jsonData.toString)

    }
    (status, message)
  }

  def prepareUserConnectionsQuery(social_user_id: String, start: Int, count: Int): (Boolean, String) = {

    var query = "http://api.linkedin.com/v1/people/" + social_user_id + "/connections:(id,first-name,last-name,headline,location:(name),industry,num-connections,summary,specialties,interests,skills,educations,phone-numbers,date-of-birth,main-address,picture-url,distance,api-public-profile-request:(url),site-public-profile-request:(url),api-standard-profile-request:(headers),public-profile-url,relation-to-viewer:(distance))"

    var status = false
    var message = "Query for getting user connections has error."
    if ((start > 0) && (count > 0)) {
      query += "?start=" + start + "&count=" + count
    }
    if (social_user_id != "") {
      status = true
      logger.debug("Q for user connections is:" + query)
      return (status, query)
    }
    (status, message)
  }

  def saveUserConnections(email: String, social_user_id: String, content: String): (Boolean, String) = {
    var dataList: Elem = null
    var status = false
    var message = ""

    if (simulateXmlLoadingForTest) {
      try {
        val source = scala.io.Source.fromFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_user_connections_full.xml")
        //data = source.mkString
        //data = XML.loadFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_profile.xml")
        dataList = XML.loadString(source.mkString.replaceAll("&", "&amp;"))
        logger.debug("LinkedIn user connections XML - " + dataList)
        //source.close()
      } catch {
        case e: IOException =>
          e.printStackTrace()
          message = e.getMessage
          logger.error("Error occurred in reading profile xml file:" + message)
      }
    } else {
      dataList = XML.loadString(content)
    }

    val (s, m) = checkXmlErrors(dataList)
    if (s == false)
      return (s, m)

    if ((dataList != "") && (s == true)) {
      for (data <- (dataList \\ "person")) {

        val id = (data \ "id").text

        var noconn = -1
        if (!(data \ "num-connections").isEmpty)
          noconn = (data \ "num-connections").text.toInt

	logger.debug("Trying to save user connections for user ID - " + id)
        var usp = UserSocialProfile.createRecord

        val uspList = UserSocialProfile.findAll(com.mongodb.QueryBuilder.start("social_user_id").is(id).get)
        if (uspList.length == 0) {
          logger.debug("User with social_user_id=" + id + " do not exists. So, add user information.")

        } else {
          logger.debug("User with social_user_id=" + id + " already exists. So, update its information.")
          usp = uspList(0)
        }

        //val usp = UserSocialProfile.createRecord

        usp.username_email.set(email)
        usp.social_user_id.set(id.toString)
        usp.connection_user_id.set(social_user_id) // - pawan
        usp.first_name.set((data \ "first-name").text)
        usp.last_name.set((data \ "last-name").text)
        usp.headline.set((data \ "headline").text)
        usp.location.set((data \ "location" \ "name").text)
        usp.industry.set((data \\ "industry").text)
        usp.no_connections.set(noconn)
        usp.interests.set((data \\ "interests").text)

	if ((data \ "date-of-birth" \ "year").text != "") { 
        var year = (data \ "date-of-birth" \ "year").text.toInt
        var month = (data \ "date-of-birth" \ "month").text.toInt
        var day = (data \ "date-of-birth" \ "day").text.toInt
        logger.debug("DOB of user is " + year)

        usp.dob.set(Scalendar(year = year, month = month, day = day))

	}
        usp.main_address.set((data \\ "main-address").text)
        usp.picture_url.set((data \\ "picture-url").text)
        usp.public_profile_url.set((data \\ "public-profile-url").text)
      val phone = (data \\ "phone-number" \ "phone-number").text
      usp.phone_nos.set(phone)
      usp.phones.set(List(Phones(((data \\ "phone-number" \ "phone-number").text), (data \\ "phone-number" \ "phone-type").text)))
      usp.educations.set((List(Educations((data \\ "education" \ "id").text, (data \\ "education" \ "school-name").text, (data \\ "education" \ "start-date" \ "year").text, (data \\ "education" \ "end-date" \ "year").text))))
      usp.extended_profile.set(List(ExtendedProfile((data \\ "summary").text, (data \\ "specialties").text)))
      usp.summary.set((data \\ "summary").text)
      usp.specialities.set((data \\ "specialties").text)

        //usp.phones.set(List(Phones(((data \\ "phone-number" \ "phone-number").text), (data \\ "phone-number" \ "phone-type").text)))
        //usp.educations.set((List(Educations((data \\ "education" \ "id").text, (data \\ "education" \ "school-name").text, (data \\ "education" \ "start-date" \ "year").text, (data \\ "education" \ "end-date" \ "year").text))))
        //logger.debug("education information extracted from xml for the user")
        //usp.extended_profile.set(List(ExtendedProfile((data \\ "summary").text, (data \\ "specialties").text)))
        usp.graph_distance.set((data \\ "distance").text.toInt)
        usp.api_std_profile_http_name.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "name").text)
        usp.api_std_profile_http_value.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "value").text)
        usp.action_type.set("CONNECTIONS")
        usp.save

	val user_conn_map = UserConnectionMapping.createRecord
	user_conn_map.social_user_id.set(id)
	user_conn_map.connection_user_id.set(social_user_id)
	user_conn_map.is_active.set(true)
	user_conn_map.is_deleted.set(false)
	user_conn_map.save
        logger.debug("LinkedIn user connection ID - " + id.toString + " has no of connections - " + noconn.toString)
      }
      message = "All profile data has been saved."
      status = true
    }
    (status, message)
  }


  def savePeopleSearch(email: String, content: String): (Boolean, String) = {

    var dataList: Elem = null
    var status = false
    var message = ""
      if (showResponseData)
	logger.debug("Content to be saved is:" + content)

    if (simulateXmlLoadingForTest) {
      try {
        val source = scala.io.Source.fromFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_people_search_api_detail.xml")
        //data = source.mkString
        //data = XML.loadFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_profile.xml")
        dataList = XML.loadString(source.mkString.replaceAll("&", "&amp;"))
        //logger.debug("LinkedIn user connections XML - " + dataList)
        //source.close()
      } catch {
        case e: IOException =>
          e.printStackTrace()
          message = e.getMessage
          logger.error("Error occurred in reading profile xml file:" + message)
      }
    } else {
      //var sb:StringBuilder = scala.xml.Utility.unescape(content, new StringBuilder)
      dataList = XML.loadString(content.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&quot;","'").replaceAll("&","&amp;").replaceAll("<data>","").replaceAll("</data>","").trim)
    }

    val (s, m) = checkXmlErrors(dataList)
    if (s == false)
      return (s, m)

    //logger.info("People search data [" + dataList.toString() + "]is to be saved now...")
    if ((dataList != null) && (s == true)) {
      logger.debug("Saving people search data which is valid...")
      if ((dataList \\ "people-search").isEmpty) { 
	status = false
	message = "Invalid or empty XML found."
	return (status, message)
      }

      for (data <- (dataList \\ "person")) {
        var id = ""
        var noconn = 0
        var usp = UserSocialProfile.createRecord

	if (!(data \\ "id").isEmpty)
	  id = (data \\ "id").text
	logger.info("Saving people search record for id=" + id)

	if (!(data \\ "num-connections").isEmpty)
	  noconn = (data \\ "num-connections").text.toInt

      if (id != "0") { 
        val uspList = UserSocialProfile.findAll(com.mongodb.QueryBuilder.start("social_user_id").is(id).get)
        if (uspList.length == 0) {
          logger.debug("User with social_user_id=" + id + " do not exists. So, add user information.")

        } else {
          logger.debug("User with social_user_id=" + id + " already exists. So, update its information.")
          usp = uspList(0)
        }
      }

        //UserSocialProfile.createRecord
        usp.username_email.set(email)
        usp.social_user_id.set(id.toString)
        usp.connection_user_id.set("")
        usp.first_name.set((data \\ "first-name").text)
        usp.last_name.set((data \\ "last-name").text)
        usp.headline.set((data \\ "headline").text)
        usp.location.set((data \ "location" \ "name").text)
        usp.industry.set((data \\ "industry").text)
        usp.no_connections.set(noconn)
        usp.interests.set((data \\ "interests").text)
	if (!(data \ "date-of-birth" \ "year").isEmpty) { 
        val year = (data \ "date-of-birth" \ "year").text.toInt
        val month = (data \ "date-of-birth" \ "month").text.toInt
        val day = (data \ "date-of-birth" \ "day").text.toInt
        usp.dob.set(Scalendar(year = year, month = month, day = day))
	}
        usp.main_address.set((data \\ "main-address").text)
        usp.picture_url.set((data \\ "picture-url").text)
        usp.public_profile_url.set((data \\ "public-profile-url").text)

	  if (!(data \\ "phone-number" \ "phone-number").isEmpty) { 
	    val phone = (data \\ "phone-number" \ "phone-number").text
	    usp.phone_nos.set(phone)
	    usp.phones.set(List(Phones(((data \\ "phone-number" \ "phone-number").text), (data \\ "phone-number" \ "phone-type").text)))
	  }
	

	if (!(data \\ "education").isEmpty){ 
      usp.educations.set((List(Educations((data \\ "education" \ "id").text, (data \\ "education" \ "school-name").text, (data \\ "education" \ "start-date" \ "year").text, (data \\ "education" \ "end-date" \ "year").text))))

	}
      usp.extended_profile.set(List(ExtendedProfile((data \\ "summary").text, (data \\ "specialties").text)))
      usp.summary.set((data \\ "summary").text)
      usp.specialities.set((data \\ "specialties").text)

        //usp.phones.set(List(Phones(((data \\ "phone-number" \ "phone-number").text), (data \\ "phone-number" \ "phone-type").text)))
        //usp.educations.set((List(Educations((data \\ "education" \ "id").text, (data \\ "education" \ "school-name").text, (data \\ "education" \ "start-date" \ "year").text, (data \\ "education" \ "end-date" \ "year").text))))
        //logger.debug("education information extracted from xml for the user")
        //usp.extended_profile.set(List(ExtendedProfile((data \\ "summary").text, (data \\ "specialties").text)))
	if (!(data \\ "distance").isEmpty) { 
        usp.graph_distance.set((data \\ "distance").text.toInt)
	}

        usp.api_std_profile_http_name.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "name").text)
        usp.api_std_profile_http_value.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "value").text)

        usp.api_std_profile_http_name.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "name").text)
        usp.api_std_profile_http_value.set((data \ "api-standard-profile-request" \ "headers" \ "http-header" \ "value").text)
        usp.action_type.set("PEOPLESEARCH")
        usp.save
        logger.debug("LinkedIn user people search ID - " + id.toString + " has no of connections - " + noconn.toString)

      }
      status = true
      message = "People search data for the user with email - " + email + " has been saved."

    }
    (status, message)
  }

  def checkXmlErrors(dataList: Elem): (Boolean, String) = {
    //var dataList: Elem = null
    var status = true
    var message = ""
    //dataList = XML.loadString(data)
    if (!(dataList \\ "status").isEmpty) { 
if ((dataList \\ "status").text != "200") {
      message = (dataList \\ "message").text

      message = "Oops! Invalid API Request."
      logger.error(message)
      status = false
    }
      else { 
	status = true
      }
    
    }
    (status, message)
  }

  def sendInvitationEmail(accessToken: Token, apiUrl: String, payload: String): (Boolean, String) = {
    var status = false
    var message = ""
    logger.debug("Sending invitation using URL:" + apiUrl)
    val request: OAuthRequest = new OAuthRequest(Verb.POST, apiUrl);

    request.addPayload(payload);

    request.addHeader("Content-Length", Integer.toString(payload.length()));
    //request.addHeader("Content-Type", "text/xml");
    request.addHeader("Content-Type", "text/plain; charset=ISO-8859-1");

    service.signRequest(accessToken, request);
    val response: Response = request.send();
    if (response.getCode().equals("200")) {
      status = true
      message = response.getBody()
      logger.debug("Message length of send invitation email is:" + message.length())
    } else {
      message = "Oops! Cannot send invitation email to LinkedIn connection. Message from server:" + response.getBody()
      logger.error("Error occurred in sending invitation email - " + message)
    }
    (status, message)

  }

  def prepareInvitationEmail(content: String, emailObj: SocialEmailEntity, apiCred: LinkedInXmlCredentialEntity): (Boolean, String) = {
    var status = false
    var message = ""
    if ((apiCred.apiHeaderID == "") && (apiCred.apiHeaderValue == "")) {
      message = "Xml access codes for user_social_id=" + emailObj.to_social_user_id + " are empty"
      return (status, message)
    }

/*
            <person path={ idvalue }>
              <first-name>{ emailObj.to_first_name }</first-name>
              <last-name>{ emailObj.to_last_name }</last-name>
            </person>
  */  
    val idvalue = "/people/id=" + emailObj.to_social_user_id
    val payload: NodeSeq =
      <mailbox-item>
        <recipients>
          <recipient>
            <person path={idvalue} />
          </recipient>
        </recipients>
        <subject>{emailObj.subject}</subject>
        <body>{emailObj.body}
        </body>
        <item-content>
          <invitation-request>
            <connect-type>friend</connect-type>
            <authorization>
              <name>{apiCred.apiHeaderValue.split(":")(0)}</name>
              <value>{apiCred.apiHeaderValue.split(":")(1)}</value>
            </authorization>
          </invitation-request>
        </item-content>
      </mailbox-item>

    status = true
    //If api cred are valid then send the payload and status back with success
    (status, payload.toString())

  }

  def preparePeopleSearchQuery(input: PeopleSearchEntity, start: Int, count: Int): (Boolean, String) = {
    var query = "http://api.linkedin.com/v1/people-search:(people:(id,first-name,last-name,headline,location:(name),industry,num-connections,summary,specialties,interests,skills,educations,phone-numbers,date-of-birth,main-address,picture-url,distance,api-public-profile-request:(url),site-public-profile-request:(url),api-standard-profile-request:(headers),public-profile-url))"
    var ex = false
    var q = ""
    if (input.keywords != "") {
      q = q + "keywords=" + input.keywords + "&"
      ex = true
    }
    if (input.role != "") { 
      q = q + "title=" + input.role + "&current-title=false&"
      ex = true
    }
    if (input.first_name != "") {
      q = q + "first-name=" + input.first_name + "&"
      ex = true
    }
    if (input.last_name != "") {
      q = q + "last-name=" + input.last_name + "&"
      ex = true
    }

    if (input.company_name != "") {
      q = q + "company-name=" + input.company_name
      q = q + "&current-company=false&"
      ex = true
    }

    if (input.industry != "") { 
      q = q + "facet=industry," + input.industry + "&"
      ex = true
    }

    if (input.degree != "") {
      input.degree match {
        case "1" =>
          q = q + "facet=network,F&"
          ex = true
        case "2" =>
          q = q + "facet=network,S&"
          ex = true
        case "4" =>
          q = q + "facet=network,A&"
          ex = true
        case "3" =>
          q = q + "facet=network,O&"
          ex = true
        case "0" =>
          logger.debug("Self serach.")
        case _ =>

      }
      ex = true
    }
    if (ex == true) {
      query = query + "?" + q.substring(0, q.length - 1)
      if (count > 0) {
        query += "&start=" + start + "&count=" + count
      }
      logger.debug("Q for people-search LinkedIn is:" + query)
      return (true, query)
    } else
      (false, "Either invalid or no query parameters found.")
  }
  //	http://api.linkedin.com/v1/companies::(162479,universal-name=linkedin)
  //

  def prepareCompanySearchQuery(input: PeopleSearchEntity, start: Int, count: Int): (Boolean, String) = {

    var query = "http://api.linkedin.com/v1/company-search:(companies:(id,name,universal-name,website-url,industries,status,logo-url,blog-rss-url,twitter-id,employee-count-range,specialties,locations,description,stock-exchange,founded-year,end-year,num-followers),facets)?start=" + start + "&count=" + count

    var q = ""
    var status = false
    var message = ""
    
    if (input.company_name != "") {
      q = q + "&keywords=" + input.company_name 
    }
    if (input.industry != "") {
      q = q + "&facet=industry," + input.industry
    }
    if (q != "") { 
      query = query + q
      //query = query.substring(0, query.length-1)
      logger.debug("Q for company search LinkedIn is:" + query)
      status = true
      return (status, query)
    }

    (false, "Either compnay name is not provided or company name is empty.")
  }


  def saveCompanySearchResults(email: String, social_user_id: String, content: String): (Boolean, String) = {
    var dataList: Elem = null
    var status = false
    var message = ""

    if (simulateXmlLoadingForTest) {
      try {
        val source = scala.io.Source.fromFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_company_search_detail.xml")
        dataList = XML.loadString(source.mkString.replaceAll("&", "&amp;"))
        logger.debug("LinkedIn company profile  XML - " + dataList)
        //source.close()
      } catch {
        case e: IOException =>
          e.printStackTrace()
          message = e.getMessage
          logger.error("Error occurred in reading company profile xml file:" + message)
      }
    } else {
      dataList = XML.loadString(content.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&quot;","'").replaceAll("&","&amp;").replaceAll("<data>","").replaceAll("</data>","").trim)
      //dataList = XML.loadString(content)
    }
    val (s, m) = checkXmlErrors(dataList)
    if (s == false)
      return (s, m)

    if ((dataList != null) && (s == true)) {
      for (data <- (dataList \\ "company")) {
        val id = (data \\ "id").text
        var cp = CompanyProfile.createRecord
      if (id != "0") { 
        val uspList = CompanyProfile.findAll(com.mongodb.QueryBuilder.start("social_company_id").is(id).get)
        if (uspList.length == 0) {
          logger.debug("Company with social_company_id=" + id + " do not exists. So, add company information.")

        } else {
          logger.debug("Company with social_company_id=" + id + " already exists. So, update its information.")
          cp = uspList(0)
        }
      }
        cp.social_company_id.set((data \\ "id").text)
        cp.company_name.set((data \\ "name").text)
        cp.universal_name.set((data \\ "universal-name").text)
        cp.website_url.set((data \\ "website-url").text)
	var industry = ""
	if (!(data \\ "industry").isEmpty) { 
	  for(i <- (data \\ "industry")) { 
	    industry += (i \ "name").text + ","
	  }
	  if (industry != "")
	    industry = industry.substring(0, industry.length-1)
          cp.industry.set(industry)
	}

        logger.debug("Getting location information for company..")
        for (l <- (data \\ "locations" \ "location")) {
          cp.location.set((l \ "address" \ "street1").text + ", " + (l \ "address" \ "city").text + ", " + (l \ "address" \ "postal-code").text)
        }

        cp.no_followers.set((data \\ "num-followers").text.toInt)
        cp.emp_count_range.set((data \\ "employee-count-range" \ "name").text)
        var spl = ""
        logger.debug("Getting specialities information for company..")
        for (s <- (data \\ "specialties" \ "specialty")) {
          spl = spl + s.text + ", "
        }
        spl = spl.substring(0, spl.length - 2)
        cp.specialities.set(spl)
        var phones = ""
        logger.debug("Getting contact phones information for company..")
        for (c <- (data \\ "locations" \ "location" \ "contact-info")) {
          phones = phones + (c \ "phone1").text + ", "

        }
        cp.contact_nos.set(phones.substring(0, phones.length - 2))
        cp.status.set((data \\ "status" \ "name").text)
        cp.description.set((data \\ "description").text)
        cp.save


	var user_conn_map = UserCompany.createRecord
        val cList = UserCompany.findAll(com.mongodb.QueryBuilder.start("social_company_id").is(id).put("connection_user_id").is(social_user_id).get)
	if (cList.length == 1)
	  user_conn_map = cList(0)

	user_conn_map.social_company_id.set(id)
	user_conn_map.connection_user_id.set(social_user_id)
	user_conn_map.is_active.set(true)
	user_conn_map.is_deleted.set(false)
	user_conn_map.updated_on(Scalendar.now)
	user_conn_map.save

	logger.debug("User with ID - %s has ")
        logger.info("Company profile with ID - " + id + " is saved")
      }
      status = true
      message = "Compnay profile(s) has been updated."
      logger.debug(message)

    }
    (status, message)
  }


  def prepareCompanyProfileQuery(input: PeopleSearchEntity, start: Int, count: Int): (Boolean, String) = {

    var query = "http://api.linkedin.com/v1/companies::(#####):(id,name,universal-name,company-type,website-url,square-logo-url,industry,status,employee-count-range,specialties,locations,num-followers,description)"

    var q = ""
    var status = false
    var message = ""
    if (input.company_name != "") {
      val toks = input.company_name.split(",")
      toks.length match {
        case 1 =>
          q = "universal-name=" + toks(0) + ","
        case _ =>
          toks.map(a => q = q + "universal-name=" + a.trim + ",")
      }
      q = q.substring(0, q.length - 1)
      query = query.replace("#####", q)
      if ((start > 0) && (count > 0)) {
        query += "?start=" + start + "&count=" + count
      }

      logger.debug("Q for company search LinkedIn is:" + query)
      status = true
      return (status, query)
    }

    (false, "Either compnay name is not provided or company name is empty.")
  }

  def saveCompanyProfile(email: String, social_user_id: String, content: String): (Boolean, String) = {
    var dataList: Elem = null
    var status = false
    var message = ""

    if (simulateXmlLoadingForTest) {
      try {
        val source = scala.io.Source.fromFile("/opt/workspace/root/eFoundry.in/LinkedInAPITest/src/main/resources/toserve/data/linkedin_company_search.xml")
        dataList = XML.loadString(source.mkString.replaceAll("&", "&amp;"))
        logger.debug("LinkedIn company profile  XML - " + dataList)
        //source.close()
      } catch {
        case e: IOException =>
          e.printStackTrace()
          message = e.getMessage
          logger.error("Error occurred in reading company profile xml file:" + message)
      }
    } else {
      dataList = XML.loadString(content)
    }
    val (s, m) = checkXmlErrors(dataList)
    if (s == false)
      return (s, m)

    if ((dataList != "") && (s == true)) {
      for (data <- (dataList \\ "company")) {
        val id = (data \\ "id").text
        var cp = CompanyProfile.createRecord
      if (id != "0") { 
        val uspList = CompanyProfile.findAll(com.mongodb.QueryBuilder.start("social_company_id").is(id).get)
        if (uspList.length == 0) {
          logger.debug("Company with social_company_id=" + id + " do not exists. So, add company information.")

        } else {
          logger.debug("Company with social_company_id=" + id + " already exists. So, update its information.")
          cp = uspList(0)
        }
      }
        cp.social_company_id.set((data \\ "id").text)
        cp.company_name.set((data \\ "name").text)
        cp.universal_name.set((data \\ "universal-name").text)
        cp.website_url.set((data \\ "website-url").text)
        cp.industry.set((data \\ "industry").text)
        logger.debug("Getting location information for company..")
        for (l <- (data \\ "locations" \ "location")) {
          cp.location.set((l \ "address" \ "street1").text + ", " + (l \ "address" \ "city").text + ", " + (l \ "address" \ "postal-code").text)
        }

        cp.no_followers.set((data \\ "num-followers").text.toInt)
        cp.emp_count_range.set((data \\ "employee-count-range" \ "name").text)
        var spl = ""
        logger.debug("Getting specialities information for company..")
        for (s <- (data \\ "specialties" \ "specialty")) {
          spl = spl + s.text + ", "
        }
        spl = spl.substring(0, spl.length - 2)
        cp.specialities.set(spl)
        var phones = ""
        logger.debug("Getting contact phones information for company..")
        for (c <- (data \\ "locations" \ "location" \ "contact-info")) {
          phones = phones + (c \ "phone1").text + ", "

        }
        cp.contact_nos.set(phones.substring(0, phones.length - 2))
        cp.status.set((data \\ "status" \ "name").text)
        cp.description.set((data \\ "description").text)
        cp.save


	var user_conn_map = UserCompany.createRecord
        val cList = UserCompany.findAll(com.mongodb.QueryBuilder.start("social_company_id").is(id).put("connection_user_id").is(social_user_id).get)
	if (cList.length == 1)
	  user_conn_map = cList(0)

	user_conn_map.social_company_id.set(id)
	user_conn_map.connection_user_id.set(social_user_id)
	user_conn_map.is_active.set(true)
	user_conn_map.is_deleted.set(false)
	user_conn_map.updated_on(Scalendar.now)
	user_conn_map.save

	logger.debug("User with ID - %s has ")
        logger.info("Company profile with ID - " + id + " is saved")
      }
      status = true
      message = "Compnay profile(s) has been updated."
      logger.debug(message)

    }
    (status, message)
  }


}


object LinkedInConstants { 
  val LINKEDIN_INDUSTRY_TABLE = Map(""->"-----------------", "1"->"Test")
}
