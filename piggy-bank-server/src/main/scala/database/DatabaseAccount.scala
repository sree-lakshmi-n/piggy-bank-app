package database

import java.sql.{Connection, Date, PreparedStatement, ResultSet}
import org.postgresql.util.PGobject
import java.time.LocalDate
import DatabaseConnection.*

object DatabaseAccount:
  val maxTransactionLimit: Double = 150000.0



  def checkBalance(connection: Connection, account_num: Int): Double =
    val query = "select * from account where account_num=?"
    val stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setInt(1, account_num)
    val rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      val balance = rs.getDouble("balance")
      println(balance)
      balance
    else
      println("No data found")
      -1.0

  def addDatatoTransaction(connection: Connection, account_num: Int, amount: Double, transactType: String,
                           transactStatus: String): Int =
    val query = "insert into transaction (transaction_type,amount,date,account_num,status) values(?,?,?,?,?) returning transaction_id"
    val stmt = connection.prepareStatement(query)
    val currentDate = Date.valueOf(LocalDate.now())
    val typeObject = new PGobject
    typeObject.setType("transaction_type_enum")
    typeObject.setValue(transactType)
    val statusObject = new PGobject
    statusObject.setType("transaction_status_enum")
    statusObject.setValue(transactStatus)
    stmt.setObject(1, typeObject)
    stmt.setDouble(2, amount)
    stmt.setDate(3, currentDate)
    stmt.setInt(4, account_num)
    stmt.setObject(5, statusObject)
    var rs: ResultSet = stmt.executeQuery()
    var transactionId = -1
    if (rs.next()) {
      transactionId = rs.getInt("transaction_id")
      println(s"Inserted row(s) into transaction")
    }
    transactionId

  def addDatatoTransfer(connection: Connection, recipient_account_num: Int, transaction_id: Int): Unit =
    val query = "insert into transfer (recipient_account_num,transaction_id) values(?,?) returning transaction_id"
    val stmt = connection.prepareStatement(query)
    stmt.setInt(1, recipient_account_num)
    stmt.setInt(2, transaction_id)
    stmt.execute()
    println(s"Inserted row(s) into transfer")

  def updateAccountBalance(connection: Connection, account_num: Int, finalAmount: Double): Unit =
    val query = "update account set balance=? where account_num=?"
    val stmt = connection.prepareStatement(query)
    stmt.setDouble(1, finalAmount)
    stmt.setInt(2, account_num)
    val rowsAffected = stmt.executeUpdate()
    println(s"Updated $rowsAffected row(s) in account")

  def deposit(connection: Connection, account_num: Int, amount: Double): Boolean =
    var query = "select * from account where account_num=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setInt(1, account_num)
    var rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      val balance = rs.getDouble("balance")
      println(balance + amount)
      val transactionId = addDatatoTransaction(connection, account_num, amount, "deposit", "success")
      updateAccountBalance(connection, account_num, (balance + amount))
      true
    else
      println("Account not found")
      false

  def withdraw(connection: Connection, account_num: Int, amount: Double): (Boolean, String) =
    var query = "select * from account where account_num=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setInt(1, account_num)
    var rs: ResultSet = stmt.executeQuery()
    if (rs.next()) then
      val balance = rs.getDouble("balance")
      if balance < amount then
        val transactionId = addDatatoTransaction(connection, account_num, amount, "withdrawal", "fail")
        (false, "No sufficient balance")
      else if (getTodayTransactedAmount(connection, account_num) + amount) >= maxTransactionLimit then
        val transactionId = addDatatoTransaction(connection, account_num, amount, "withdrawal", "fail")
        (false, "Transaction limit reached")
      else
        val transactionId = addDatatoTransaction(connection, account_num, amount, "withdrawal", "success")
        updateAccountBalance(connection, account_num, (balance - amount))
        (true, "Amount withdrawn successfully")
    else
      (false, "Account not found")


  def transferMoney(connection: Connection, upi_id: String, amount: Double, recipient_upi_id: String): (Boolean, String) =
    var account_num = getAccountNum(connection, upi_id)
    var query = "select * from account where account_num=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setInt(1, account_num)
    var rs: ResultSet = stmt.executeQuery()
    var transactionId = -1
    if (rs.next()) then
      val balance = rs.getDouble("balance")
      if balance < amount then
        transactionId = addDatatoTransaction(connection, account_num, amount, "transfer", "fail")
        (false,"No sufficient balance")
      else if (getTodayTransactedAmount(connection, account_num) + amount) >= maxTransactionLimit then
        val transactionId = addDatatoTransaction(connection, account_num, amount, "transfer", "fail")
        (false,"Transaction limit reached")
      else
        query = "select * from account where account_num=?"
        stmt = connection.prepareStatement(query)
        val recipient_account_num: Int = getAccountNum(connection, recipient_upi_id)
        stmt.setInt(1, recipient_account_num)
        rs = stmt.executeQuery()
        if (rs.next()) then
          val recipientBalance = rs.getDouble("balance")
          transactionId = addDatatoTransaction(connection, account_num, amount, "transfer", "success")
          addDatatoTransfer(connection, recipient_account_num, transactionId)
          updateAccountBalance(connection, account_num, balance - amount)
          updateAccountBalance(connection, recipient_account_num, recipientBalance + amount)
          (true,"Amount transferred successfully")
        else
          transactionId = addDatatoTransaction(connection, account_num, amount, "transfer", "fail")
          (false, "Recipient UPI id invalid")
    else
      (false,"Account not found")

  def getUpiId(connection: Connection, accountNum: Int): String =
    var query = "select * from account where account_num=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setInt(1, accountNum)
    var rs: ResultSet = stmt.executeQuery()
    var upiid = ""
    if (rs.next()) then
      upiid = rs.getString("upi_id")
    upiid

  def getAccountNum(connection: Connection, upiId: String): Int =
    var query = "select * from account where upi_id=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setString(1, upiId)
    var rs: ResultSet = stmt.executeQuery()
    var accountNum = -1
    if (rs.next()) then
      accountNum = rs.getInt("account_num")
    accountNum

  def getAccountNumFromCustId(connection: Connection, cust_id: Int): Int =
    var query = "select * from account where cust_id=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setInt(1, cust_id)
    var rs: ResultSet = stmt.executeQuery()
    var accountNum = -1
    if (rs.next()) then
      accountNum = rs.getInt("account_num")
    accountNum

  def getAccountDetails(connection: Connection, upiId: String): (Int,Int,Double) =
    var query = "select * from account where upi_id=?"
    var stmt: PreparedStatement = connection.prepareStatement(query)
    stmt.setString(1, upiId)
    var rs: ResultSet = stmt.executeQuery()
    var accountNum = -1
    var custId = -1
    var balance = -1
    if (rs.next()) then
      accountNum = rs.getInt("account_num")
      custId = rs.getInt("cust_id")
      balance = rs.getInt("balance")
    (accountNum,custId,balance)

  def getTodayTransactedAmount(connection: Connection, account_num: Int): Double =
    val query = "select sum(amount) as total from transaction where (transaction_type=? or transaction_type=?)  and account_num=? and status=? and date=CURRENT_DATE"
    val stmt: PreparedStatement = connection.prepareStatement(query)
    val withdrawObject = new PGobject
    withdrawObject.setType("transaction_type_enum")
    withdrawObject.setValue("withdrawal")
    val transferObject = new PGobject
    transferObject.setType("transaction_type_enum")
    transferObject.setValue("transfer")
    val statusObject = new PGobject
    statusObject.setType("transaction_status_enum")
    statusObject.setValue("success")
    stmt.setObject(1, withdrawObject)
    stmt.setObject(2, transferObject)
    stmt.setInt(3, account_num)
    stmt.setObject(4, statusObject)
    var rs: ResultSet = stmt.executeQuery()
    if rs.next() then
      rs.getDouble("total")
    else
      0.0



