package code.utils


object Constants {
  //localhost or remote
  val EMAIL_TYPE = "localhost"
  val LINKEDIN_SELF_PROFILE = "http://api.linkedin.com/v1/people/~:(id,first-name,last-name,headline,location:(name),industry,num-connections,summary,specialties,interests,skills,educations,phone-numbers,date-of-birth,main-address,picture-url,distance,api-public-profile-request:(url),site-public-profile-request:(url),api-standard-profile-request:(headers),public-profile-url,three-current-positions,three-past-positions)"
  val LINKEDIN_CONNECTIONS = "http://api.linkedin.com/v1/people/~/connections:(id,last-name)"
  val LINKEDIN_USER_CONNECTIONS = "http://api.linkedin.com/v1/people/~/connections:(id,first-name,last-name,headline,location:(name),industry,num-connections,summary,specialties,interests,skills,educations,phone-numbers,date-of-birth,main-address,picture-url,distance,api-public-profile-request:(url),site-public-profile-request:(url),api-standard-profile-request:(headers),public-profile-url,relation-to-viewer:(distance)"
  val LINKEDIN_PEOPLE_SEARCH = "http://api.linkedin.com/v1/people-search:(people:(id,first-name,last-name,headline,location:(name),num-connections,phone-numbers,picture-url,distance,relation-to-viewer:(distance)))?start=0&count=15&keywords=abcdefgh"
  val LINKEDIN_PEOPLE_SEARCH_WITH_API_DETAIL = "http://api.linkedin.com/v1/people-search:(people:(id,first-name,last-name,headline,location:(name),num-connections,phone-numbers,picture-url,distance,relation-to-viewer:(distance),api-standard-profile-request))?start=0&count=15&keywords=abcdefgh"

  val LINKEDIN_PEOPLE_SEARCH_0 = "http://api.linkedin.com/v1/people-search:(people:(id,first-name,last-name,headline,location:(name),num-connections,phone-numbers,picture-url,distance,relation-to-viewer:(distance)))?start=0&count=15&keywords=abcdefgh&school-name=jdhjdfdd"
val LINKEDIN_COMPANIES = "http://api.linkedin.com/v1/companies::(162479,universal-name=linkedin):(id,name,universal-name,company-type,website-url,square-logo-url,industry,status,employee-count-range,specialties,locations,num-followers,description)"
val LINKEDIN_COMPANY_SEARCH = "http://api.linkedin.com/v1/company-search:(companies:(id,name,universal-name,website-url,industries,status,logo-url,blog-rss-url,twitter-id,employee-count-range,specialties,locations,description,stock-exchange,founded-year,end-year,num-followers),facets)?facet=industry,5&keywords=cisco"
}
