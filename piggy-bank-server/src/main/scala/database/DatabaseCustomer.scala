package database

import java.sql.{Connection, PreparedStatement, ResultSet}
import DatabaseConnection.*

object DatabaseCustomer:
  def registerCustomer(connection: Connection, name: String, email: String, password: String, phone_number: String, upi_id: String): Boolean =
    if (checkCustomerExists(connection, phone_number, email) && checkUpiExists(connection, upi_id)) then
      println("check false")
      false
    else
      addCustomer(connection, name, email, password, phone_number, upi_id)
      println("check true")
      true

  def isColValueUnique(connection: Connection, custId: Int, column: String, colValue: String): Boolean =
    var query = s"select * from customer where $column=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setString(1, colValue)
    var rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      val cust_id = rs.getInt("cust_id")
      if cust_id != custId then
        println("false")
        false
      else
        true
    else
      true


  def updateContactDetails(connection: Connection, custId: Int, email: String, phone_number: String): Boolean =
    if (isColValueUnique(connection, custId, "mobile_num", phone_number) &&
      isColValueUnique(connection, custId, "email", email)) then
      var query = "update customer set email=?,mobile_num=? where cust_id=?"
      var stmt: PreparedStatement = connection.prepareStatement(query)
      stmt.setString(1, email)
      stmt.setString(2, phone_number)
      stmt.setInt(3, custId)
      val rowsAffected = stmt.executeUpdate()
      println(s"Inserted $rowsAffected row(s) into Customers")
      stmt.close()
      true
    else
      println("Entered credentials already belong to another account")
      false

  def checkCustomerExists(connection: Connection, phone_num: String, email: String): Boolean =
    var query = "select * from customer where mobile_num=? or email=?"
    var stmt = connection.prepareStatement(query)
    stmt.setString(1, phone_num)
    stmt.setString(2, email)
    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      true
    else
      false

  def checkUpiExists(connection: Connection, upi_id: String): Boolean =
    var query = "select * from account where upi_id=?"
    var stmt = connection.prepareStatement(query)
    stmt.setString(1, upi_id)
    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      true
    else
      false

  def getCustomerDetails(connection: Connection, cust_id: Int): (String, String, String) =
    var query = "select * from customer where cust_id=?"
    var stmt = connection.prepareStatement(query)
    stmt.setInt(1, cust_id)
    val rs: ResultSet = stmt.executeQuery()
    var name = ""
    var email = ""
    var mobile_num = ""
    var password = ""
    if (rs.next()) then
      name = rs.getString("name")
      email = rs.getString("email")
      mobile_num = rs.getString("mobile_num")
    (name, email, mobile_num)

  def addCustomer(connection: Connection, name: String, email: String, password: String, phone_number: String, upi_id: String): Unit = {
    var query = "insert into customer(name, email, mobile_num, password) values(?,?,?,?) returning cust_id"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setString(1, name)
    stmt.setString(2, email)
    stmt.setString(3, phone_number)
    stmt.setString(4, password)
    val rs: ResultSet = stmt.executeQuery()
    println("add")
    var custId = -1
    if (rs.next()) {
      custId = rs.getInt("cust_id")
      println(s"Inserted row(s) into customer")
    }

    if (custId >= 0) then
      query = "insert into account(cust_id,balance,upi_id) values(?,?,?)"
      stmt = connection.prepareStatement(query)
      stmt.setInt(1, custId)
      stmt.setDouble(2, 0.0)
      stmt.setString(3, upi_id)
      val rowsAffected = stmt.executeUpdate()
      println(s"Inserted $rowsAffected row(s) into account")
    stmt.close()
  }

  def loginCustomer(connection: Connection, custId: Int, password: String): Boolean =
    var query = "select * from customer where cust_id=? and password=?"
    var stmt = connection.prepareStatement(query)
    stmt.setInt(1, custId)
    stmt.setString(2, password)
    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      query = "insert into session values(?,?)"
      stmt = connection.prepareStatement(query)
      val sessionId = java.util.UUID.randomUUID.toString
      stmt.setString(1, sessionId)
      stmt.setInt(2, custId)
      val rowsAffected = stmt.executeUpdate()
      println(s"Inserted $rowsAffected row(s) into session")
      true
    else
      false

  def isCustIdValid(connection: Connection, custId: Int): Boolean =
    val query = "select * from customer where cust_id=?";
    val stmt = connection.prepareStatement(query)
    stmt.setInt(1, custId)
    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      true
    else
      false

  def logoutCustomer(connection: Connection, custId: Int): Boolean =
    if (isCustIdValid(connection, custId)) then
      val query = "delete from session where cust_id=?";
      val stmt = connection.prepareStatement(query)
      stmt.setInt(1, custId)
      val rowsAffected = stmt.executeUpdate()
      println("logout successful!")
      println(s"Deleted $rowsAffected row(s) from session")
      true
    else
      println("Invalid session")
      false

  def getCustId(connection: Connection, sessionId: String): Int =
    val query = "select * from session where session_id=?";
    val stmt = connection.prepareStatement(query)
    stmt.setString(1, sessionId)
    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      rs.getInt("cust_id")
    else
      -1  

