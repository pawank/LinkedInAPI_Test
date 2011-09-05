package code.model

import net.liftweb.mongodb.record.{MongoMetaRecord, MongoRecord}
import com.github.philcali.scalendar.conversions._
import com.github.philcali.scalendar._
import com.github.philcali.scalendar.Scalendar._
import net.liftweb.record.field.{BooleanField, DateTimeField, StringField}
import net.liftweb.mongodb.record.field.{UUIDField, ObjectIdPk}
import collection.immutable.HashMap
import net.liftweb.common.Full
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.{BasicDBObjectBuilder, BasicDBObject, DBObject}

class Setting private() extends MongoRecord[Setting] with ObjectIdPk[Setting]{

  def meta = Setting

  //def id = _id.value
  //object _id extends UUIDField(this)


  object name extends StringField(this, 30)
  object name_value extends StringField(this, 100)
  object created_on extends DateTimeField(this, Scalendar.now)
  object is_deleted extends BooleanField(this, false)
}


object Setting extends Setting with MongoMetaRecord[Setting] {
  private var globalSystemSettingCache = new HashMap[String,String]()

  def load() {

    /*
    Setting.createRecord
      .name("version")
      .name_value("1.0")
      .created_on(Scalendar.now)
      .save


    import com.mongodb.{BasicDBObject, BasicDBObjectBuilder, DBObject}
    val dbo = BasicDBObjectBuilder.start("name", "version").get
    val v = Setting.find(dbo)
        for (ll <- v) {
          ll.name_value("test")
          ll.save
        }
    */
    val l:List[Setting] = Setting.findAll
    l.map(i =>

      globalSystemSettingCache += i.name.value -> i.name_value.value)
  }

  def getValue(key:String):Option[String] = {
        globalSystemSettingCache.get(key)
  }

  def getValueAsString(key:String) = {
    getValue(key) match {
      case Some(v) =>
        v
      case _ =>
        ""
    }
    ""
  }
}