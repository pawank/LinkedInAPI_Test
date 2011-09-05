package code.snippet

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import Helpers._
import _root_.scala.xml._
import net.liftweb.mapper._
class Start extends StatefulSnippet  {
  
  var (name, password, message) = ("", "", "")
  var isChecked = false
  var dispatch: DispatchIt = {
    case "init" => beginwork _
  }
  
  def beginwork(xhtml: NodeSeq): NodeSeq = {
    import code.linkedin.LinkedInConfig

    val linkedinconf = new LinkedInConfig
    linkedinconf.service
    //linkedinconf.processOAuth

    import code.model._
    import net.liftweb.json.JsonDSL._
    println(Setting.findAll(("name" -> "version") ~ ("name_value" -> "1.0")).toString())

    Setting.createRecord
      .name("linkedin_api_key")
      .name_value("S4fKadvFpa1MP-X-sJmwRa1mSdB98_hk9z_PwzdnUWeKzqi3vjDzUm7vbGtEWO6Y")
      .save

    Setting.createRecord
      .name("linkedin_secret_key")
      .name_value("ZpUTUjhb6rLDmDrSBxmmORAd-ld9HH4QWvi0JFAGx0_cfrFo-0-R1lz5UEbEgMKY")
      .save

    xhtml
  }
}
