/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code.utils

import net.liftweb.util.Mailer
import Mailer._
import _root_.scala.xml._
import org.slf4j.LoggerFactory

import net.liftweb.common.Full
import javax.mail.internet._
import java.util.Properties._
import code.utils._
import ch.qos.logback.core.joran.conditional.ElseAction
import javax.mail._

object Mail {
  def logger = LoggerFactory.getLogger("GeneralUtils")

  private def sendMailUsingSendmail(from: String, to: String, subject: String, body: String) {
    val session = Session.getDefaultInstance(System.getProperties)
    val message = new MimeMessage(session)
    // Set the from, to, subject, body text
    message.setFrom(new InternetAddress(from))
    message.setRecipients(Message.RecipientType.TO, to)
    message.setSubject(subject)
    message.setText(body)
    // And send it
    Transport.send(message)
  }

  private def sendPlainEmail(from: String, to: String, subject: String, body: String) {
    logger.info("Sending email from (" concat  from concat  ") " +
      "to (" concat  to concat
      ") with subject ("
        + subject + ")..")
    Constants.EMAIL_TYPE match {
      case "localhost" =>
        sendMailUsingSendmail(from, to, subject, body)
        logger.info("Email sent to:" + to + " with subject:" + subject)
      case "remote" =>
        sendMail(From(from),
          Subject(subject),
          To(to),
          PlainMailBodyType(body))
      case _ =>
        logger.error("Invalid EMAIL_TYPE found.")
    }

    logger.info("Sent email from (" + from + ") to (" + to + ") with subject (" + subject + ") using EMAIL_TYPE (" + Constants.EMAIL_TYPE + ")")

  }

  private def sendXhtmlEmail(from: String, to: String, subject: String, body: NodeSeq) {
    logger.info("Sending email from (" + from + ") to (" + to + ") with subject (" + subject + ")..")
    Constants.EMAIL_TYPE match {
      case "localhost" =>
        sendMailUsingSendmail(from, to, subject, body.toString())
      case "remote" =>
        sendMail(From(from),
          Subject(subject),
          To(to),
          XHTMLPlusImages(body))
      case _ =>
        logger.error("Invalid EMAIL_TYPE found.")
    }

    logger.info("Sent email from (" + from + ") to (" + to + ") with subject (" + subject + ") using EMAIL_TYPE (" + Constants.EMAIL_TYPE + ")")
  }


  def sendEmail(from: String, to: String, subject: String, body: NodeSeq, isHtml:Boolean = true) {
    if (isHtml) sendXhtmlEmail(from,to,subject,body) else sendMailUsingSendmail(from,to,subject,body.toString())
  }

  def configureLocalhostMailer() {
    val properties = System.getProperties
    properties.put("mail.smtp.host", "localhost")

  }


  def configureRemoteMailer(host: String, user: String, password: String) {
    // Enable TLS support
    System.setProperty("mail.smtp.starttls.enable", "true");
    // Set the host name
    System.setProperty("mail.smtp.host", host) // Enable authentication
    System.setProperty("mail.smtp.auth", "true") // Provide a means for authentication. Pass it a Can, which can either be Full or Empty
    Mailer.authenticator = Full(new Authenticator {
      override def getPasswordAuthentication = new PasswordAuthentication(user, password)
    })
  }
}
