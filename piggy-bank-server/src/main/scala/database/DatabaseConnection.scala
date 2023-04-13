package database

import java.sql.{Connection, DriverManager}

object DatabaseConnection:
  private val url = "jdbc:postgresql://localhost:5432/piggybank"
  private val username = "postgres"
  private val password = ""

  def getConnection(): Connection =
    Class.forName("org.postgresql.Driver")
    DriverManager.getConnection(url, username, password)
