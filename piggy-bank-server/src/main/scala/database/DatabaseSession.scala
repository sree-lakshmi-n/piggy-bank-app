//package database
//
//import java.sql.{Connection, DriverManager, Date, PreparedStatement, ResultSet}
//import java.time.{Duration, Instant}
//import scala.util.{Try, Success, Failure}
//import DatabaseConnection.*
//
//case class Session(sessionId: String, custId: Int, var lastAccessTime: Instant)
//
//object DatabaseSession:
//  val sessionTimeout = Duration.ofMinutes(15)
//
//  def createSession(conn: Connection, custId: Int): Session =
//    val sessionId = java.util.UUID.randomUUID().toString
//    val session = Session(sessionId, custId, Instant.now())
//    val stmt = conn.prepareStatement("INSERT INTO session (session_id, cust_id, last_access_time) VALUES (?, ?, ?)")
//    stmt.setString(1, sessionId)
//    stmt.setInt(2, custId)
//    stmt.setTimestamp(3, java.sql.Timestamp.from(session.lastAccessTime))
//    stmt.executeUpdate()
//    session
//
//  def removeSession(conn: Connection, sessionId: String): Try[Unit] =
//    val deleteStmt = conn.prepareStatement("DELETE FROM session WHERE session_id = ?")
//    deleteStmt.setString(1, sessionId)
//    Try(deleteStmt.executeUpdate()) match {
//      case Success(_) => Success(())
//      case Failure(e) => Failure(e)
//    }
//
//  def getSession(conn: Connection, sessionId: String): Option[Session] =
//    val stmt = conn.prepareStatement("SELECT cust_id, last_access_time FROM session WHERE session_id = ?")
//    stmt.setString(1, sessionId)
//    val rs = stmt.executeQuery()
//    if (rs.next()) then
//      val custId = rs.getInt("cust_id")
//      val lastAccessTime = rs.getTimestamp("last_access_time").toInstant
//      if (Duration.between(lastAccessTime, Instant.now()).compareTo(sessionTimeout) > 0) then
//        removeSession(conn, sessionId)
//        None
//      else
//        // Update session access time
//        val updateStmt = conn.prepareStatement("UPDATE session SET last_access_time = ? WHERE session_id = ?")
//        updateStmt.setTimestamp(1, java.sql.Timestamp.from(Instant.now()))
//        updateStmt.setString(2, sessionId)
//        updateStmt.executeUpdate()
//        Some(Session(sessionId, custId, lastAccessTime))
//    else
//      None
//
//  def getSessionByCustId(conn: Connection, custId: Int): Option[Session] =
//    val selectStmt = conn.prepareStatement("SELECT session_id, last_access_time FROM session WHERE cust_id = ?")
//    selectStmt.setInt(1, custId)
//    val rs = selectStmt.executeQuery()
//    if (rs.next()) then
//      val sessionId = rs.getString("session_id")
//      val lastAccessTime = rs.getTimestamp("last_access_time").toInstant
//      Some(Session(sessionId, custId, lastAccessTime))
//    else
//      None
