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

    case class XmlAccessCodes(id: String, value: String) extends JsonObject[XmlAccessCodes] {
      def meta = XmlAccessCodes
    }

    object XmlAccessCodes extends JsonObjectMeta[XmlAccessCodes]


    case class ExtendedProfile(summary: String, specialities: String) extends JsonObject[ExtendedProfile] {
      def meta = ExtendedProfile
    }

    object ExtendedProfile extends JsonObjectMeta[ExtendedProfile]

    case class Phones(number: String, phone_type: String) extends JsonObject[Phones] {
      def meta = Phones
    }

    object Phones extends JsonObjectMeta[Phones]

    case class Educations(edu_id: String, school_name: String, start_date: String, end_date: String) extends JsonObject[Educations] {
      def meta = Educations
    }
    object Educations extends JsonObjectMeta[Educations]


    class Phone private () extends MongoRecord[Phone] with ObjectIdPk[Phone] {
      def meta = Phone
      object phone_no extends StringField(this, 15)
      object phone_type extends StringField(this, 20)
    }

    object Phone extends Phone with MongoMetaRecord[Phone] {
    }

    class UserSocialProfile private () extends MongoRecord[UserSocialProfile] {
      def logger = LoggerFactory.getLogger("UserSocialProfile")

      def meta = UserSocialProfile

      def id = _id.value

      object _id extends UUIDField(this)

      object username_email extends EmailField(this, 100)

      object social_user_id extends StringField(this, 30) {
        override def validations =
          meta.valUnique("User social identifier should be unique") _ ::
            valMinLen(6, "User social ID cannot be less than 6 characters") _ :: super.validations
      }

      object connection_user_id extends StringField(this, 30)

      object first_name extends StringField(this, 50)

      object last_name extends StringField(this, 50)

      object headline extends StringField(this, 200)

      //object email_address extends StringField(this, 100)
      object location extends StringField(this, 100)

      object industry extends StringField(this, 100)

      object no_connections extends IntField(this)

      object graph_distance extends IntField(this)

      object interests extends StringField(this, 300)

      object dob extends DateField(this)

      object main_address extends StringField(this, 400)

      object picture_url extends StringField(this, 200)

      object public_profile_url extends StringField(this, 200)

      object api_std_profile_http_name extends StringField(this, 50)
      object api_std_profile_http_value extends StringField(this, 50)

      object phones extends MongoJsonObjectListField(this, Phones)
      object educations extends MongoJsonObjectListField(this, Educations)
      object extended_profile extends MongoJsonObjectListField(this, ExtendedProfile)
            //object access_codes extends MongoJsonObjectListField(this, XmlAccessCodes)

	object phone_nos extends StringField(this, 30)
	object education_exp extends StringField(this, 300)
	object summary extends StringField(this, 1000)
	object specialities extends StringField(this, 500)

	object company_with_position extends StringField(this,500)
	
      object action_type extends StringField(this, 20)

      object created_on extends DateTimeField(this, Scalendar.now)

      object updated_on extends DateTimeField(this)

      object is_deleted extends BooleanField(this, false)

      object is_active extends BooleanField(this, true)

            object is_connected extends BooleanField(this, false)
      protected def valUnique(msg: String)(value: String): List[FieldError] = {

        Nil
      }
    }

    object UserSocialProfile extends UserSocialProfile with MongoMetaRecord[UserSocialProfile] {
      override def collectionName = "user_social_profile"

      override def valUnique(msg: String)(value: String): List[FieldError] = {
        logger.debug("Overloaded.")
        if (this.find("userName", value).isDefined)
          List(FieldError(this.social_user_id, msg))
        else
          Nil
      }

      //Get record with children based on social_user_id
      def getUserSocialProfileList(social_user_id: String): List[UserSocialProfile] = {
        import com.mongodb._
        val users = UserSocialProfile.findAll(QueryBuilder.start("social_user_id").is(social_user_id).get)

        for (x <- users) {
          logger.info("social_user_id of user profile is " + x.social_user_id.is + " and ID is " + x.id.toString())
        }
        users
      }

    }

  }

}
