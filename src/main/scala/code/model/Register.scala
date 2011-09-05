/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package code {

  package model {
    import _root_.net.liftweb.mapper._
    import _root_.net.liftweb.util._
    import _root_.net.liftweb.common._
    

    class Register extends LongKeyedMapper[Register] {
      def getSingleton = Register

      def primaryKeyField = id

      object id extends MappedLongIndex(this)

      object username extends MappedString(this,100)
      object password extends MappedString(this,50)
      object first_name extends MappedString(this,50)
      object last_name extends MappedString(this,50)
      object email extends MappedEmail(this,100)
      object phone extends MappedString(this,20) {
        override def defaultValue = "0000000000"
      }
      object city extends MappedString(this,100)
      object country extends MappedString(this,100) {
        override def defaultValue = "IN"
      }
      object address extends MappedString(this,200)
      object updated_by extends MappedString(this,50) {
        override def defaultValue = "SYSTEM"
      }
      object is_active extends MappedBoolean(this) {
        override def defaultValue = false
      }
      object is_registered extends MappedBoolean(this) {
        override def defaultValue = false
      }
      object created_on extends MappedDateTime(this)
      object updated_on extends MappedDateTime(this)
      object key extends MappedString(this,50)
    }
    
    object Register extends Register with LongKeyedMetaMapper[Register] {
                              override def dbTableName = "register" // define the DB table name
    }

  }
}
