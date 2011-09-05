package code.model
import net.liftweb._
import mongodb._
import util.Props
import com.mongodb.{Mongo, ServerAddress}

object AdminDb extends MongoIdentifier {
  val jndiName = "admin"
}

//object MarketingOfficeDb extends MongoIdentifier {
//  val jndiName = "mo"
//}

object MongoDBConfig {
  def init: Unit = {
    val srvr = new ServerAddress(
       Props.get("mongo.host", "127.0.0.1"),
       Props.getInt("mongo.port", 27017)
    )
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr), "mo")
    //MongoDB.defineDb(MarketingOfficeDb, new Mongo(srvr), "mo")
    MongoDB.defineDb(AdminDb, new Mongo(srvr), "admin")
  }
}
