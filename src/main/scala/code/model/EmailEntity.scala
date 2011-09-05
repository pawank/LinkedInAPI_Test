package code.model { 

  case class EmailEntity(to_first_name: String, to_last_name: String, to_email_address:String, to_social_id: String, from_first_name:String, from_last_name:String, from_email_address: String, subject: String, body: String, ccList: List[String], bccList: List[String]) 

  case class SocialEmailEntity(to_first_name: String, to_last_name: String, to_email_address:String,from_first_name:String, from_last_name:String, from_email_address: String, subject: String, body: String, ccList: List[String], bccList: List[String], to_social_user_id:String, from_social_user_id:String) 
}
