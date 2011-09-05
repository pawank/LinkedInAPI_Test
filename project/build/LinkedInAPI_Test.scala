import sbt._
import org.netbeans.plugins._



class LinkedInAPITestProject(info: ProjectInfo) extends DefaultWebProject(info) with SbtNetbeansPlugin {
  val liftVersion = "2.4-M3"

  // uncomment the following if you want to use the snapshot repo
  //  val scalatoolsSnapshot = ScalaToolsSnapshots

	val javaDotNetRepo = "Java.net Repo" at "http://download.java.net/maven/2/" 
  // If you're using JRebel for Lift development, uncomment
  // this line
  // override def scanDirectories = Nil
  //val scalaQuery = "org.scalaquery" %% "scalaquery" % "0.9.4"
  val scalendar = "com.github.philcali" %% "scalendar" % "0.0.5"

  val mysql = "mysql" % "mysql-connector-java" % "5.1.12"

    val logback_core = "ch.qos.logback" % "logback-core" % "0.9.26" % "compile" //LGPL 2.1
      val logback_classic = "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile" //LGPL 2.1
      val log4j_over_slf4j = "org.slf4j" % "log4j-over-slf4j" % "1.6.1"
val poi = "org.apache.poi" % "poi" % "3.7"



  val mongo = "org.mongodb" % "mongo-java-driver" % "1.4" % "compile->default"

  val jetty7Server = "org.eclipse.jetty" % "jetty-server" % "7.0.1.v20091125" % "compile,test -> default"
  val jetty7WebApp = "org.eclipse.jetty" % "jetty-webapp" % "7.0.1.v20091125" % "compile,test -> default"

  val lift_mongo = "net.liftweb" %% "lift-mongodb" % liftVersion
  val lift_mongo_record = "net.liftweb" %% "lift-mongodb-record" % liftVersion
  val lift_facebook = "net.liftweb" %% "lift-facebook" % liftVersion
  val lift_widgets = "net.liftweb" %% "lift-widgets" % liftVersion
  val lift_wizard = "net.liftweb" %% "lift-wizard" % liftVersion
  val lift_oauth = "net.liftweb" %% "lift-oauth" % liftVersion
  val lift_json = "net.liftweb" %% "lift-json" % liftVersion

  val scribe = "org.scribe" %% "scribe" % "1.1.2"

	val rogue = "com.foursquare" %% "rogue" % "1.0.19"

  //override unmanagedJars = file("/opt/workspace/OSS/PlayLinkedIn/lib/scribe-1.1.0.jar")
  //lazy val barProject = project("bar-project", "bar-project", new BarProject(_))

  //class BarProject(info: ProjectInfo) extends DefaultWebProject(info) with SbtNetbeansPlugin

  override def libraryDependencies = Set(
	"net.liftweb" %% "lift-webkit" % liftVersion % "compile",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile",
    "junit" % "junit" % "4.5" % "test",
    "org.scala-tools.testing" %% "specs" % "1.6.8" % "test",
  "org.mortbay.jetty" % "jetty" % "6.1.25" % "test->default"
  ) ++ super.libraryDependencies




}
