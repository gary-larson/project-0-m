package com.revature.project_0

import java.sql.DriverManager
import java.sql.Connection
import java.sql.SQLException

class DatabaseUtil {

    def getConnection(): Option[Connection] = {
        val connectString = "jdbc:postgresql://localhost:5433/covid"
        val user = "postgres"
        val password = "postgres"
        try {
            val conn = DriverManager.getConnection(connectString, user, password)
            println("Connection successful")
            Some(conn)
        } catch {
            case ex: Exception => {
                println(s"Connection failed: ${ex.getMessage()}")
                None
            }
        }
    }

    def disconnect(conn: Connection): Boolean = {
        try {
            conn.close()
            true
        } catch {
            case sql: SQLException => {
                false
            }
        }
    }
}