package code.linkedin
import java.util.Date
import org.scribe.oauth.OAuthService
import org.scribe.builder.api.LinkedInApi
import org.scribe.builder.ServiceBuilder
import org.slf4j.LoggerFactory
import code.model.Setting
import xml.NodeSeq
import org.scribe.model._
import net.liftweb.http.{RequestVar, S}

class LinkedInConfig   {
  def logger = LoggerFactory.getLogger("LinkedInConfig")
  object requestTokenSession extends RequestVar[String]("")
  object requestTokenSecretSession extends RequestVar[String]("")

// Values stored in the session
  val KEY_REQUEST_TOKEN = "requestToken"
  val KEY_REQUEST_TOKEN_SECRET = "requestTokenSecret"
  val KEY_ACCESS_TOKEN = "accessToken"
  val KEY_ACCESS_TOKEN_SECRET = "accessTokenSecret"


    //scribe
    val service:OAuthService = {
      new ServiceBuilder()
        .provider(classOf[LinkedInApi])
        //.apiKey(Setting.getValueAsString("linkedin_api_key"))
        //.apiSecret(Setting.getValueAsString("linkedin_secret_key"))
        .apiKey("S4fKadvFpa1MP-X-sJmwRa1mSdB98_hk9z_PwzdnUWeKzqi3vjDzUm7vbGtEWO6Y")
        .apiSecret("ZpUTUjhb6rLDmDrSBxmmORAd-ld9HH4QWvi0JFAGx0_cfrFo-0-R1lz5UEbEgMKY")
        .callback("http://localhost:8080/dashboard")
        .build()
    }

  def getLinkedInAuthorizationUrl:String = {
      // Obtain the Request Token
          logger.debug("Fetching the Request Token...");
          val requestToken:Token = service.getRequestToken();
          logger.debug("Got the Request Token!");

          logger.debug("Now go and authorize Scribe here:");
          val url = service.getAuthorizationUrl(requestToken);

          requestTokenSession(requestToken.getToken())
          requestTokenSecretSession(requestToken.getSecret())
          //S.set(KEY_REQUEST_TOKEN, requestToken.getToken())
          //S.set(KEY_REQUEST_TOKEN_SECRET, requestToken.getSecret())
          logger.info("Redirecting to " + url + "\n\n")

          //S.redirectTo(url)
          url
  }

  def processOAuth(oauth_verifier:String):NodeSeq = {
          logger.debug("=== LinkedIn's OAuth Workflow ===");

          logger.info("Redirected from LinkedIn with oauth_verifier " + oauth_verifier)
        val token = requestTokenSession.is
        val secret = requestTokenSecretSession.is
        logger.info("Rebuilding with request token        " + token)
        logger.info("Rebuilding with request token secret " + secret)
          val requestToken = new Token(token, secret)


          val verifier = new Verifier(oauth_verifier)
          val accessToken:Token = service.getAccessToken(requestToken, verifier);
          logger.debug("Got the Access Token!");
          logger.debug("(if your curious it looks like this: " + accessToken + " )");

          var xml = ""
          // Now let's go and ask for a protected resource!
          logger.debug("Now we're going to access a protected resource...");
          val  PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~/connections:(id,last-name)"
          val request:OAuthRequest = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
          service.signRequest(accessToken, request);
          val response:Response = request.send();
          logger.debug("Got it! Lets see what we found...");

          xml = response.getBody
          logger.debug(response.getBody());

          logger.debug("Thats it man! Go and build something awesome with Scribe! :)");

          <root>{xml}</root>
    }



  /*
  def processOAuthCommandLine {
        import java.util.Scanner
        var in:Scanner = new Scanner(System.in);

        logger.debug("=== LinkedIn's OAuth Workflow ===");
        logger.debug();

        // Obtain the Request Token
        logger.debug("Fetching the Request Token...");
        val requestToken:Token = service.getRequestToken();
        logger.debug("Got the Request Token!");
        logger.debug();

        logger.debug("Now go and authorize Scribe here:");
        logger.debug(service.getAuthorizationUrl(requestToken));
        logger.debug("And paste the verifier here");
        System.out.print(">>");
        val verifier:Verifier = new Verifier(in.nextLine());
        logger.debug();

        // Trade the Request Token and Verfier for the Access Token
        logger.debug("Trading the Request Token for an Access Token...");
        val accessToken:Token = service.getAccessToken(requestToken, verifier);
        logger.debug("Got the Access Token!");
        logger.debug("(if your curious it looks like this: " + accessToken + " )");
        logger.debug();

        // Now let's go and ask for a protected resource!
        logger.debug("Now we're going to access a protected resource...");
        val  PROTECTED_RESOURCE_URL = "http://api.linkedin.com/v1/people/~/connections:(id,last-name)"
        val request:OAuthRequest = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
        service.signRequest(accessToken, request);
        val response:Response = request.send();
        logger.debug("Got it! Lets see what we found...");
        logger.debug();
        logger.debug(response.getBody());

        logger.debug();
        logger.debug("Thats it man! Go and build something awesome with Scribe! :)");

  }*/



}
