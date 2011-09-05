/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code.snippet

import _root_.net.liftweb.util._
import Helpers._
import _root_.scala.xml._
import code.model._
import org.slf4j.LoggerFactory
import net.liftweb.common.Full
import net.liftweb.http._
import js.JsCmds.{ReplaceOptions, After}
import js.{JsCmd, JE}
import SHtml._

class Register extends StatefulSnippet {
  def logger = LoggerFactory.getLogger("Register")

  var country = Register.country
  var city = ""


  var dispatch: DispatchIt = {
    case "joinus" => registerForm _
    case "register" => registerForm _
    //case "register/activate" => activateForm _
  }

  var (username, password, cpassword, first_name, last_name, mobile, email, address) = ("", "", "", "", "", "", "", "")


  private def cityChoice(state: String): Elem = {
    val cities = Register.citiesFor(state)
    val first = cities.head
    // make the select "untrusted" because we might put new values
    // in the select
    untrustedSelect(cities.map(s => (s, s)), Full(first), s => city = s)
  }

  private def replace(state: String): JsCmd = {
    val cities = Register.citiesFor(state)
    val first = cities.head
    ReplaceOptions("city_select", cities.map(s => (s, s)), Full(first))
  }


  def activateForm(xhtml: NodeSeq): NodeSeq = {
    //S notice <i>a</i>
    <i>
      {S.params("key")}
    </i>
  }


  def activate(key: String): Boolean = {
    var x: Boolean = false
    x
  }

  def registerForm(xhtml: NodeSeq): NodeSeq = {
    var key: String = S.param("key").openOr("")
    val (name, js) = ajaxCall(JE.JsRaw("this.value"),
      s => After(200, replace(s)))

    if (key != "") {
      var actstatus = activate(key)
      if (actstatus) {

        return <i>Your account has been activated. Click
          <a href="http://localhost:8080/login">here</a>
          to login.</i>
      }
      else
        return <i>Your registration link has expired or invalid. Please register again.</i>
    }


    var errors = ""



    def processMe() {
      logger.info("Processing signup/register/joinus request...")
      var status = false

      //errors ::: username :: password :: cpassword :: first_name :: last_name :: email :: mobile :: country :: Nil
      if (username == "") {
        errors = errors + "Username cannot be empty.<br/>"
      }
      if (password == "") {
        errors = errors + "Password cannot be empty.<br/>"
      }
      if (cpassword == "") {
        errors = errors + "Confirmation password cannot be empty.<br/>"
      }
      if (first_name == "") {
        errors = errors + "First name cannot be empty.<br/>"
      }
      if (last_name == "") {
        errors = errors + "Last name cannot be empty.<br/>"
      }
      if (mobile == "") {
        errors = errors + "Mobile no cannot be empty.<br/>"
      }
      if (country == "") {
        errors = errors + "Please select your country.<br/>"
      }
      if (city == "") {
        errors = errors + "Please select your city.<br/>"
      }
      println(errors.toString())
      if (errors.length == 0) {
        status = true
      }

      var message = ""

      if (status) {
        logger.debug("Saving user information into the database for email - " + email)
        var res: Tuple2[Boolean, String] = User.register(email, password, first_name, last_name, mobile, country, city)
        status = res._1
        message = res._2
        errors = message + errors
      }

      if (status) {
        //S notice <b>Please follow instructions sent to you email address, <b>{email}</b> to activate your account.</b>
        S notice <div class="errormsg">Thanks for registration. Your account is ready to use. Click
          <a href="/">here</a>
          to login.</div>
      }
      else {
        //S error <div class="error">{message}{errors}</div>
        logger.error("Setting the error message to - " + errors)
        S notice <div class="errormsg">
          {errors}
        </div>
      }
    }

    logger.info("Binding joinus parameters..")

    //"#message*" #>  message + errors


    bind("r", xhtml,
      "username" -> SHtml.text(username, username = _, "id" -> "username"),
      "password" -> SHtml.password(password, password = _, "id" -> "password"),
      "cpassword" -> SHtml.password(cpassword, cpassword = _, "id" -> "cpassword"),
      "first_name" -> SHtml.text(first_name, first_name = _, "id" -> "first_name"),
      "last_name" -> SHtml.text(last_name, last_name = _, "id" -> "last_name"),
      "mobile" -> SHtml.text(mobile, mobile = _, "id" -> "mobile"),
      "email" -> SHtml.text(email, email = _, "id" -> "email", "type" -> "email"),
      "address" -> SHtml.textarea(address, address = _, "id" -> "address", "rows" -> "6"),
      //"country" -> SHtml.text(country, country = _, "id"->"country"),
      "country" -> select(Register.countries.map(s => (s, s)),
        Full(country), s => country = s, "onchange" -> js.toJsCmd) %
        (new PrefixedAttribute("lift", "gc", name, Null)),
      "city" -> cityChoice(country) % ("id" -> "city_select"),
      "submit" -> SHtml.submit("REGISTER", processMe, "id" -> "submit")
    )
  }
}


object Register {
  val citiesAndStates = List("India" -> "IXA,Agartala",
    "India" -> "AGR,Agra",
    "India" -> "AGX,Agatti Island",
    "India" -> "AMD,Ahmedabad",
    "India" -> "AJL,Aizawl",
    "India" -> "IXD,Allahabad",
    "India" -> "ATQ,Amritsar",
    "India" -> "IXU,Aurangabad",
    "India" -> "IXB,Bagdogra",
    "India" -> "BLR,Bangalore",
    "India" -> "IXG,Belgaum",
    "India" -> "BEP,Bellary",
    "India" -> "BHU,Bhavnagar",
    "India" -> "BHO,Bhopal",
    "India" -> "BBI,Bhubaneshwar",
    "India" -> "BHJ,Bhuj",
    "India" -> "BOM,Bombay",
    "India" -> "CCU,Calcutta",
    "India" -> "CCJ,Calicut",
    "India" -> "IXC,Chandigarh",
    "India" -> "MAA,Chennai",
    "India" -> "COK,Cochin",
    "India" -> "CJB,Coimbatore",
    "India" -> "DED,Dehradun",
    "India" -> "DEL,Delhi",
    "India" -> "DHM,Dharamshala",
    "India" -> "DIB,Dibrugarh",
    "India" -> "DMU,Dimapur",
    "India" -> "DIU,Diu",
    "India" -> "GAY,Gaya",
    "India" -> "GOI,Goa",
    "India" -> "GOP,Gorakhpur",
    "India" -> "GAU,Guwahati",
    "India" -> "GWL,Gwalior",
    "India" -> "HBX,Hubli",
    "India" -> "HYD,Hyderabad",
    "India" -> "IMF,Imphal",
    "India" -> "IDR,Indore",
    "India" -> "JLR,Jabalpur",
    "India" -> "JAI,Jaipur",
    "India" -> "IXW,Jamshedpur",
    "India" -> "IXJ,Jammu",
    "India" -> "JGA,Jamnagar",
    "India" -> "JDH,Jodhpur",
    "India" -> "JRH,Jorhat",
    "India" -> "IXY,Kandla",
    "India" -> "KNU,Kanpur",
    "India" -> "HJR,Khajuraho",
    "India" -> "KLH,Kolhapur",
    "India" -> "CCU,Kolkata",
    "India" -> "KUU,Kullu",
    "India" -> "LTU,Latur",
    "India" -> "IXL,Leh",
    "India" -> "IXI,Lilabari",
    "India" -> "LKO,Lucknow",
    "India" -> "LUH,Ludhiana",
    "India" -> "MAA,Madras",
    "India" -> "IXM,Madurai",
    "India" -> "IXE,Mangalore",
    "India" -> "BOM,Mumbai",
    "India" -> "MYQ,Mysore",
    "India" -> "NAG,Nagpur",
    "India" -> "ISK,Nasik",
    "India" -> "NDC,Nanded",
    "India" -> "DEL,New Delhi",
    "India" -> "PGH,Pantnagar",
    "India" -> "IXP,Pathankot",
    "India" -> "PAT,Patna",
    "India" -> "PBD,Porbandar",
    "India" -> "IXZ,PortBlair",
    "India" -> "PNQ,Pune",
    "India" -> "RPR,Raipur",
    "India" -> "RJA,Rajahmundry",
    "India" -> "RAJ,Rajkot",
    "India" -> "IXR,Ranchi",
    "India" -> "SXV,Salem",
    "India" -> "SHL,Shillong",
    "India" -> "SSE,Sholapur",
    "India" -> "SLV,Shimla",
    "India" -> "IXS,Silchar",
    "India" -> "SXR,Srinagar",
    "India" -> "STV,Surat",
    "India" -> "TEZ,Tezpur",
    "India" -> "TRZ,Tiruchirapally",
    "India" -> "TIR,Tirupati",
    "India" -> "TRV,Trivandrum",
    "India" -> "TCR,Tuticorin",
    "India" -> "UDR,Udaipur",
    "India" -> "BDQ,Vadodara",
    "India" -> "VNS,Varanasi",
    "India" -> "VGA,Vijaywada",
    "India" -> "VTZ,Vishakhapatnam",
    "Alabama, USA" -> "Birmingham",
    "Alabama, USA" -> "Huntsville",
    "Alabama, USA" -> "Mobile",
    "Alabama, USA" -> "Montgomery",
    "Alaska" -> "Anchorage municipality",
    "Arizona, USA" -> "Chandler",
    "Arizona, USA" -> "Gilbert town",
    "Arizona, USA" -> "Glendale",
    "Arizona, USA" -> "Mesa",
    "Arizona, USA" -> "Peoria",
    "Arizona, USA" -> "Phoenix",
    "Arizona, USA" -> "Scottsdale",
    "Arizona, USA" -> "Tempe",
    "Arizona, USA" -> "Tucson",
    "Arkansas" -> "Little Rock",
    "California, USA" -> "Anaheim",
    "California, USA" -> "Antioch",
    "California, USA" -> "Bakersfield",
    "California, USA" -> "Berkeley",
    "California, USA" -> "Burbank",
    "California, USA" -> "Chula Vista",
    "California, USA" -> "Concord",
    "California, USA" -> "Corona",
    "California, USA" -> "Costa Mesa",
    "California, USA" -> "Daly City",
    "California, USA" -> "Downey",
    "California, USA" -> "El Monte",
    "California, USA" -> "Elk Grove",
    "California, USA" -> "Escondido",
    "California, USA" -> "Fairfield",
    "California, USA" -> "Fontana",
    "California, USA" -> "Fremont",
    "California, USA" -> "Fresno",
    "California, USA" -> "Fullerton",
    "California, USA" -> "Garden Grove",
    "California, USA" -> "Glendale",
    "California, USA" -> "Hayward",
    "California, USA" -> "Huntington Beach",
    "California, USA" -> "Inglewood",
    "California, USA" -> "Irvine",
    "California, USA" -> "Lancaster",
    "California, USA" -> "Long Beach",
    "California, USA" -> "Los Angeles",
    "California, USA" -> "Modesto",
    "California, USA" -> "Moreno Valley",
    "California, USA" -> "Norwalk",
    "California, USA" -> "Oakland",
    "California, USA" -> "Oceanside",
    "California, USA" -> "Ontario",
    "California, USA" -> "Orange",
    "California, USA" -> "Oxnard",
    "California, USA" -> "Palmdale"
  )

  val countries = citiesAndStates.map(_._1).distinct

  val country: String = countries.head

  def citiesFor(state: String): List[String] = citiesAndStates.filter(_._1 == state).map(_._2)


}
