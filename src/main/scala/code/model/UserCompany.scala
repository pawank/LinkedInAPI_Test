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

    class UserCompany private () extends MongoRecord[UserCompany] {
      def logger = LoggerFactory.getLogger("UserCompany")

      def meta = UserCompany

      def id = _id.value

      object _id extends UUIDField(this)

      object social_company_id extends StringField(this, 30)
      object connection_user_id extends StringField(this, 30)

      object created_on extends DateTimeField(this, Scalendar.now)

      object updated_on extends DateTimeField(this)

      object is_deleted extends BooleanField(this, false)

      object is_active extends BooleanField(this, true)

    }

    object UserCompany extends UserCompany with MongoMetaRecord[UserCompany] {
      override def collectionName = "user_company"

    }


  }

}
