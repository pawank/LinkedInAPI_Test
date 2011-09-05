package code.linkedin

import java.util.Date
import net.liftweb.oauth.OAuthAccessor._
import net.liftweb.oauth.HMAC_SHA1._
import net.liftweb.http.GetRequest
import net.liftweb.oauth._

/**
 * Created by IntelliJ IDEA.
 * User: root
 * Date: 8/3/11
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */

case class FakeConsumer(consumerKey:String, consumerSecret:String) extends OAuthConsumer {

    def reset{}

    def enabled: Int = 0

    def user: OAuthUser = new OAuthUser(){}

    def title: String = ""

    def applicationUri: String = ""

    def callbackUri: String = ""

    def xdatetime: Date = new Date(0)
  }


class LinkedInOAuthServerConfig {
  import net.liftweb.oauth.OAuthUtil.Parameter

      import net.liftweb.common.{Box,Full,Empty}
      import java.util.Date
      val oauthAccessor = OAuthAccessor(FakeConsumer("S4fKadvFpa1MP-X-sJmwRa1mSdB98_hk9z_PwzdnUWeKzqi3vjDzUm7vbGtEWO6Y","ZpUTUjhb6rLDmDrSBxmmORAd-ld9HH4QWvi0JFAGx0_cfrFo-0-R1lz5UEbEgMKY"), Full("pawan"), Empty)
      val hmacSha1 = HMAC_SHA1(oauthAccessor)


      val oauthMessage = new OAuthMessage(GetRequest, "http://api.linkedin.com/v1/people/~", List(
              //Parameter("file","vacation.jpg"),
              //Parameter("size","original"),
              Parameter("oauth_consumer_key","S4fKadvFpa1MP-X-sJmwRa1mSdB98_hk9z_PwzdnUWeKzqi3vjDzUm7vbGtEWO6Y"),
              Parameter("oauth_nonce","1234"),
              Parameter("oauth_signature_method","HMAC-SHA1"),
              Parameter("oauth_timestamp","1312354085"),
              Parameter("oauth_token","pawan"),
              Parameter("oauth_version","1.0")
            ))
            val baseString = hmacSha1.getBaseString(oauthMessage)
            println(hmacSha1.getSignature(baseString))



}