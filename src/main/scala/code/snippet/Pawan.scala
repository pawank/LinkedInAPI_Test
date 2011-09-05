package code.snippet

import com.foursquare.rogue.Rogue._
import java.util.regex.Pattern
import net.liftweb.mongodb.record._
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.record._
import org.bson.types._
import net.liftweb.json.JsonDSL._
import code.model._

object Pawan { 
  UserSocialProfile.where(_.first_name startsWith "Rit").fetch()
  UserSocialProfile.where(_.first_name regexWarningNotIndexed Pattern.compile("Star.*"))
}
