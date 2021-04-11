package com.revature.project_0

import java.sql.DriverManager
import java.sql.Connection

class DatabaseUtil {

    def getConnection(): Option[Connection] = {
        val connectString = "jdbc:postgresql://localhost:5432/covid"
        val user = "coviduser"
        val password = "password"
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
}