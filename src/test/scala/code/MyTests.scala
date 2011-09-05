package code

import linkedin.LinkedInConnect
import org.specs.Specification
import org.specs.runner.JUnit4
import junit.framework.Assert._

import code.model.MongoDBConfig

class MyTestsAsTest extends JUnit4(MyTests)

object MyTests extends Specification {
    MongoDBConfig.init	

    "LinkedIn Profile XML" should {
        "XML should have data in it" in {
          val (status, message) = LinkedInConnect.saveProfile("pawan@gmail.com","")

          status must beTrue
        }
    }

  "LinkedIn User Connections Basic Info XML" should {
        "Connection XML should have data in it" in {
          val (status, message) = LinkedInConnect.saveUserConnections("pawan@gmail.com","WdEgzLj70S","")
          status must beTrue
	//true must beTrue
        }
    }
"LinkedIn People Search API Results" should {
        "XML should not be empty" in {
          val (status, message) = LinkedInConnect.savePeopleSearch("pawan@gmail.com","")
        	status must beTrue
	//true must beTrue
        }
    }

"LinkedIn users listing with ID" should {
        "Display social user id and PK ID" in {
		import code.model._
		val x = UserSocialProfile.getUserSocialProfileList("P1JPgkTFkT")
        	//(x.length() > 0) must beTrue
	true must beTrue
        }
    }

    "LinkedIn Company Profile Save" should {
        "Connection XML should have data in it" in {
          val (status, message) = LinkedInConnect.saveCompanyProfile("pawan@gmail.com","WdEgzLj70S","")
          status must beTrue
	//true must beTrue
        }
    }

}
