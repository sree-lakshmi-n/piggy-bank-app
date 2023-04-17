import java.net.InetSocketAddress
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import database.{DatabaseConnection, DatabaseCustomer}
import database.DatabaseConnection.getConnection

import scala.jdk.CollectionConverters.MapHasAsScala
import scala.io.Source
import scala.jdk.CollectionConverters.ListHasAsScala
import java.sql.Connection
import database.DatabaseCustomer.*
import database.DatabaseAccount.*
import DatabaseConnection.*

import java.net._
import java.io._
import scala.io._

import io.circe.generic.auto._
import io.circe.syntax._


case class ResponseObject(code: Int, message: String)

object RestApi:
  def main(args: Array[String]): Unit =
    val server = HttpServer.create(new InetSocketAddress(8000), 0)
    var connection: Connection = null

    try
      connection = getConnection()
      println("Database connection successful.")

      server.createContext("/login", LoginHandler(connection))
      server.createContext("/logout", LogoutHandler(connection))
      server.createContext("/register", RegisterHandler(connection))
      server.createContext("/accountinfo", AccountInfoHandler(connection))
      server.createContext("/customerinfo", CustomerInfoHandler(connection))
      server.createContext("/update", UpdateDetailsHandler(connection))
      server.createContext("/balance", CheckBalanceHandler(connection))
      server.createContext("/deposit", DepositHandler(connection))
      server.createContext("/withdraw", WithdrawHandler(connection))
      server.createContext("/transfer", TransferHandler(connection))
      server.createContext("/custid", CustIdHandler(connection))
      server.createContext("/transactions", TransactionTableHandler(connection))
      server.start()
      println("Server running on port 8000")
    catch
      case e: Exception =>
        println(e)
        println("Database connection error: " + e.getMessage)

case class RegisterHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,phonenum,email,name,password,upiid")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val name = headers.get("Name").flatMap(_.asScala.headOption).get
      val email = headers.get("Email").flatMap(_.asScala.headOption).get
      val password = headers.get("Password").flatMap(_.asScala.headOption).get
      val phone_number = headers.get("Phonenum").flatMap(_.asScala.headOption).get
      val upi_id = headers.get("Upiid").flatMap(_.asScala.headOption).get
      println(s"$name $email $password $phone_number $upi_id")
      if (registerCustomer(connection, name, email, password, phone_number, upi_id)) then
        response = "Registered Successfully"
        println(response)
        code = 200
      else
        println("422")
        response = "A customer with this email id or mobile number or upi id already exists. Try logging in"
        println(response)
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    println(responseObject)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class AccountInfoHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,upiid")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val upiid = headers.get("Upiid").flatMap(_.asScala.headOption).get
      val (accountNum, custId, balance) = getAccountDetails(connection, upiid)
      response = s"accountNum:$accountNum,custId:$custId,balance:$balance"
      code = 200
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class CustomerInfoHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,custid")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val cust_id = Integer.parseInt(headers.get("Custid").flatMap(_.asScala.headOption).get)
      println(getCustomerDetails(connection, cust_id))
      val (name, email, mobile_num) = getCustomerDetails(connection, cust_id)
      response = s"name:$name,email:$email,mobile_num:$mobile_num"
      code = 200
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class LoginHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,custid,password")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val custId = Integer.parseInt(headers.get("Custid").flatMap(_.asScala.headOption).get)
      val password = headers.get("Password").flatMap(_.asScala.headOption).get
      val (isCustomerFound, sessionId) = loginCustomer(connection, custId, password)
      if (isCustomerFound) then
        val accountNumber = getAccountNumFromCustId(connection, custId)
        val upiid = getUpiId(connection, accountNumber)
        response = s"Login successful!+$accountNumber+$upiid+$sessionId"
        code = 200
      else
        response = "No such customer found"
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class LogoutHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,custid")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val custId = Integer.parseInt(headers.get("Custid").flatMap(_.asScala.headOption).get)
      if (logoutCustomer(connection, custId)) then
        response = "Logout successful!"
        code = 200
      else
        response = "Invalid Session"
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class UpdateDetailsHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,phonenum,email,custid")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val custId = Integer.parseInt(headers.get("Custid").flatMap(_.asScala.headOption).get)
      val email = headers.get("Email").flatMap(_.asScala.headOption).get
      val phone_number = headers.get("Phonenum").flatMap(_.asScala.headOption).get
      println(s"$custId $email $phone_number")
      if (updateContactDetails(connection, custId, email, phone_number)) then
        response = "Updated successfully!"
        code = 200
      else
        response = "Entered credentials already belong to another account"
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class CheckBalanceHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,accountnum")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val accountNum = Integer.parseInt(headers.get("Accountnum").flatMap(_.asScala.headOption).get)
      if (checkBalance(connection, accountNum) >= 0) then
        response = checkBalance(connection, accountNum).toString
        code = 200
      else
        response = "No data found"
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class DepositHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,accountNum,amount")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val accountNum = (headers.get("Accountnum").flatMap(_.asScala.headOption).get).toInt
      val amount = (headers.get("Amount").flatMap(_.asScala.headOption).get).toDouble
      if (deposit(connection, accountNum, amount)) then
        response = "Deposited Successfully!"
        code = 200
      else
        response = "Account not found"
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class WithdrawHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,accountNum,amount")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val accountNum = (headers.get("Accountnum").flatMap(_.asScala.headOption).get).toInt
      val amount = (headers.get("Amount").flatMap(_.asScala.headOption).get).toDouble
      val temp: (Boolean, String) = withdraw(connection, accountNum, amount)
      response = temp(1)
      if (temp(0)) then
        code = 200
      else
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class TransferHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,accountNum,upiid,recipientupiid,amount")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val upiId = (headers.get("Upiid").flatMap(_.asScala.headOption).get)
      val amount = (headers.get("Amount").flatMap(_.asScala.headOption).get).toDouble
      val recipientUpiId = (headers.get("Recipientupiid").flatMap(_.asScala.headOption).get)
      val temp: (Boolean, String) = transferMoney(connection, upiId, amount, recipientUpiId)
      response = temp(1)
      if (temp(0)) then
        code = 200
      else
        code = 422
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class CustIdHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var response = ""
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,sessionId")
      t.sendResponseHeaders(200, -1)
    if t.getRequestMethod == "POST" then
      val headers = t.getRequestHeaders.asScala.toMap
      val sessionId = (headers.get("Sessionid").flatMap(_.asScala.headOption).get)
      val custId = getCustId(connection, sessionId)
      val accountNum = getAccountNumFromCustId(connection, custId)
      if (custId >= 0) then
        code = 200
        response = s"$custId+$accountNum"
      else
        code = 422
        response = "Not found"
    else
      response = "Method not allowed"
      code = 405
    val responseObject = ResponseObject(code, response)
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()

case class TransactionTableHandler(connection: Connection) extends HttpHandler :
  def handle(t: HttpExchange): Unit =
    var code = 400
    t.getResponseHeaders().set("Access-Control-Allow-Origin", "*")
    val httpMethod = t.getRequestMethod()
    if (httpMethod.equalsIgnoreCase("OPTIONS")) then
      t.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
      t.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization,accountNum")
      t.sendResponseHeaders(200, -1)

    val headers = t.getRequestHeaders.asScala.toMap
    val accountNum = Integer.parseInt(headers.get("Accountnum").flatMap(_.asScala.headOption).get)
    val response = getTransactionTable(connection, accountNum)
    code = 200
    println(response)
    val responseObject = ResponseObject(code, response.toString())
    val json = responseObject.asJson.toString()
    t.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8")
    t.sendResponseHeaders(code, json.getBytes(StandardCharsets.UTF_8).length)
    val os = t.getResponseBody
    os.write(json.getBytes(StandardCharsets.UTF_8))
    os.close()
    t.close()
