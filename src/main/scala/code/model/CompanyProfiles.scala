/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code {

  package model {
    import java.util.UUID
    import _root_.net.liftweb.mapper._
    import _root_.net.liftweb.util._
    import _root_.net.liftweb.common._
    import org.slf4j.LoggerFactory
    import net.liftweb.json.JsonDSL._
    import net.liftweb.mongodb.record.{ MongoMetaRecord, MongoRecord }
    import net.liftweb.record.field._
    import com.github.philcali.scalendar.Scalendar
    import net.liftweb.mongodb.{ JsonObject, JsonObjectMeta }
    import net.liftweb.mongodb.record.field._
    import net.liftweb.mongodb.record.MongoId

    /*
    case class ExtendedProfile(summary: String, specialities: String) extends JsonObject[ExtendedProfile] {
      def meta = ExtendedProfile
    }

    object ExtendedProfile extends JsonObjectMeta[ExtendedProfile]
    */

    class CompanyProfile private () extends MongoRecord[CompanyProfile] {
      def logger = LoggerFactory.getLogger("CompanyProfile")

      def meta = CompanyProfile

      def id = _id.value

      object _id extends UUIDField(this)

      object username_email extends EmailField(this, 100)

      object social_company_id extends StringField(this, 30)

      object connection_user_id extends StringField(this, 30)

      object company_name extends StringField(this, 100)

      object universal_name extends StringField(this, 100)

      object location extends StringField(this, 100)

      object industry extends StringField(this, 100)

      object no_followers extends IntField(this)

      object emp_count_range extends StringField(this,10)

      object specialities extends StringField(this, 300)

      object main_address extends StringField(this, 400)

      object contact_nos extends StringField(this, 20)

      object social_logo_url extends StringField(this, 200)

      object website_url extends StringField(this, 200)

      object status extends StringField(this, 20)

      object description extends StringField(this, 1000)

      //object access_codes extends MongoJsonObjectListField(this, XmlAccessCodes)

      object created_on extends DateTimeField(this, Scalendar.now)

      object updated_on extends DateTimeField(this)

      object is_deleted extends BooleanField(this, false)

      object is_active extends BooleanField(this, true)

    }

    object CompanyProfile extends CompanyProfile with MongoMetaRecord[CompanyProfile] {
      override def collectionName = "company_social_profile"

    }

  }

}
