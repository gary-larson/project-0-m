package com.revature.project_0

import java.sql.DriverManager
import java.sql.Connection
import java.sql.SQLException

/**
  * Class for database interactions
  *
  */
class DatabaseUtil {

    /**
      * Method to get a database connection
      *
      * @return an option connection 
      */
    def getConnection(): Option[Connection] = {
        // create connection string
        val connectString = "jdbc:postgresql://localhost:5433/covid"
        // hard coded user name **BAD**
        val user = "coviduser"
        // hard coded password **BAD**
        val password = "project0"
        try {
            // attempt connection
            val conn = DriverManager.getConnection(connectString, user, password)
            Some(conn)
        } catch {
            // catch sql connection
            case ex: Exception => {
                println(s"Connection failed: ${ex.getMessage()}")
                None
            }
        }
    }

    /**
      * Method to disconnect connection
      *
      * @param conn to disconnect
      * @return success or failure
      */
    def disconnect(conn: Connection): Boolean = {
        try {
            // close connection
            conn.close()
            true
        } catch {
            // catch sql exception
            case sql: SQLException => {
                false
            }
        }
    }

    /**
      * Method to see is all tables Exist
      *
      * @param connIn to database
      * @return success or failure
      */
    def checkTables(connIn: Connection): Boolean = {
        // create control variable
        var allTables = true
        // move connection to mutable variable
        var conn = connIn;
        // test connection
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to check the states table exists
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
              "WHERE schemaname = 'public' AND tablename  = 'states');")
              // execute query
              statement.executeQuery()
              // get result set
              val results = statement.getResultSet()
              if (results.next()) {
                  // get results
                  if(!results.getBoolean("EXISTS")) {
                      allTables = false
                  }
              }
              // close statement
              statement.close()
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("States exists query error: " + se.getMessage())
                // if exception set control variable to false
                allTables = false
            }
        }
        try {
            // create prepared statement to check the state_info table exists
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
              "WHERE schemaname = 'public' AND tablename  = 'state_info');")
              // execute query
              statement.executeQuery()
              // get result set
              val results = statement.getResultSet()
              if (results.next()) {
                  // get results
                  if(!results.getBoolean("EXISTS")) {
                      allTables = false
                  }
              }
              // close statement
              statement.close()
              allTables
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("States exists query error: " + se.getMessage())
                // if exception set control variable to false
                allTables = false
            }
        }
        try {
            // create prepared statement to check the users table exists
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
              "WHERE schemaname = 'public' AND tablename  = 'users');")
              // execute query
              statement.executeQuery()
              // get result set
              val results = statement.getResultSet()
              if (results.next()) {
                  // get results
                  if(!results.getBoolean("EXISTS")) {
                      allTables = false
                  }
              }
              // close statement
              statement.close()
              allTables
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("States exists query error: " + se.getMessage())
                // if exception set control variable to false
                false
            }
        }
    }

    /**
      * Method to create any missing tables
      *
      * @param connIn to database
      * @param stateData for states and state_info tables
      */
    def createTables(connIn: Connection, stateDataList: List[USState]): Boolean = {
        // create control variable
        var allTables = true
        // move connection to mutable variable
        var conn = connIn;
        // test connection
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement for states table
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
              "WHERE schemaname = 'public' AND tablename  = 'states');")
              // execute statement
              statement.executeQuery()
              // get result set
              val results = statement.getResultSet()
              if (results.next()) {
                  // test results
                  if (!results.getBoolean("EXISTS")) {
                    // create states table
                    val isCreated = createStatesTable(conn, stateDataList)
                    if (!isCreated) {
                        // if failed set control variable to false
                        allTables = false
                    }
                  }
              }
              // close statement
              statement.close()
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("States exists query error: " + se.getMessage())
                allTables = false
            }
        }
        try {
            // create prepared statement for state info table
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
              "WHERE schemaname = 'public' AND tablename  = 'state_info');")
              // execute query
              statement.executeQuery()
              // get result set
              val results = statement.getResultSet()
              if (results.next()) {
                  // test results
                  if (!results.getBoolean("EXISTS")) {
                    // create state info table
                    val isCreated = createStateInfoTable(conn, stateDataList)
                    if (!isCreated) {
                        // if failed set control variable to false
                        allTables = false
                    }
                  }
              }
              // close statement
              statement.close()
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("States Info exists query error: " + se.getMessage())
                allTables = false
            }
        }
        try {
            // create prepared statement for users table
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
              "WHERE schemaname = 'public' AND tablename  = 'users');")
              // execute query
              statement.executeQuery()
              // get result set
              val results = statement.getResultSet()
              if (results.next()) {
                  // test results
                  if (!results.getBoolean("EXISTS")) {
                    // create table
                    val isCreated = createUsersTable(conn)
                    if (!isCreated) {
                        // if failed set control variable to false
                        allTables = false
                    }
                  }
              }
              // close statement
              statement.close()
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Users exists query error: " + se.getMessage())
                // if failed set control variable to false
                allTables = false
            }
        }
        allTables
    }

    /**
      * Method to create states table
      *
      * @param connIn to database
      * @param stateDataList for states and state_info tables
      * @return success or failure
      */
    def createStatesTable(connIn: Connection, stateDataList: List[USState]) : Boolean = {
        // move connection to a mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to create states table
            val statement = conn.prepareStatement("CREATE TABLE states (state_id serial " +
              "primary key, state text, population int8);")
            // execute statement
            statement.execute()
            // close statement
            statement.close()
            // setup loop
            for (stateData <- stateDataList) {
                // add states one at a time
                addStatesData(connIn, stateData)
                // test add
            } 
            true
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("States create query error: " + se.getMessage())
                false
            }
        }
    }
    /**
      * Method to create states info table
      *
      * @param connIn to database
      * @param stateDataList for states and state_info tables
      * @return success or failure
      */
    def createStateInfoTable(connIn: Connection, stateDataList: List[USState]) : Boolean = {
        // move connection to a mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
              // create prepared statement for state info table
              val statement = conn.prepareStatement("CREATE TABLE state_info (state_info_id serial " +
                "primary key, populationUSARank int, pcOfUSAPopulation decimal(13,5), " +
                "mortalityRate decimal(13,5), pcOfUSADeaths decimal(13,5), pcOfUSAActiveCases decimal(13, 5), " +
                "pcOfUSARecovered decimal(13, 5), pcOfUSATotalCases decimal(13, 5), " +
                "totalCases int8, newCases int8, totalDeaths int8, newDeaths int8, totalActiveCases int8, state_id int, " +
                "FOREIGN KEY (state_id) REFERENCES states(state_id));")
            // execute statement
            statement.execute()
            // close statement
            statement.close()
            // setup loop
            for (stateData <- stateDataList) {
                // add state info to state info table individually
                addStateInfoData(conn, stateData)
            }
            true
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("State Info create query error: " + se.getMessage())
                false
            }
        }
    }

    /**
      * Method to create users table
      *
      * @param connIn to database
      * @return success of failure
      */
    def createUsersTable(connIn: Connection) : Boolean = {
        // move variable to mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to create users table
            val statement = conn.prepareStatement("CREATE TABLE users (user_id serial " +
              "primary key, name text UNIQUE, last_visited date Default now(), state_offset int);")
              // execute statement
              val isCreated = statement.execute()
              // close statement
              statement.close()
              isCreated
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Users create query error: " + se.getMessage())
                false
            }
        }
    }

    /**
      * Method to add state data to states table
      *
      * @param connIn to database
      * @param stateData to add
      * @return success or failure
      */
    def addStatesData(connIn: Connection, stateData: USState): Unit = {
        // move connection to mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to insert state data
            val statement = conn.prepareStatement("INSERT INTO states VALUES(DEFAULT, ?, ?);")
            // add state parameter
            statement.setString(1, stateData.state)
            // add population parameter
            statement.setLong(2, stateData.population)
            // execute statement
            statement.execute()
            // clse statement
            statement.close()
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Add states data error: " + se.getMessage())
            }
        }
    }

    /**
      * Method to add state info to state_info table
      *
      * @param connIn to database
      * @param stateData to add
      * @return success or failure
      */
    def addStateInfoData(connIn: Connection, stateData: USState): Unit = {
        // move connection to mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // get state id from states table for foreign key
            val stateIdMaybe = getStateId(conn, stateData.state)
            // setup match
            stateIdMaybe match {
                // if have state id continue
                case Some(stateId) => {
                    // create prepared statement to insert data into state_info table
                    val statement = conn.prepareStatement("INSERT INTO state_info VALUES(" +
                      "DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")
                    // add state population rank parameter
                    statement.setInt(1, stateData.populationUSARank)
                    // add percent of US population parameter
                    statement.setDouble(2, stateData.pcOfUSAPopulation)
                    // add mortality rate parameter
                    statement.setDouble(3, stateData.mortalityRate)
                    // add percent of US deaths parameter
                    statement.setDouble(4, stateData.pcOfUSADeaths)
                    // add percent of US active cases parameter
                    statement.setDouble(5, stateData.pcOfUSAActiveCases)
                    // add percent of US recovered parameter
                    statement.setDouble(6, stateData.pcOfUSARecovered)
                    // add percent of US total cases parameter
                    statement.setDouble(7, stateData.pcOfUSATotalCases)
                    // add total case parameter
                    statement.setLong(8, stateData.totalCases)
                    // add new cases parameter
                    statement.setLong(9, stateData.newCases)
                    // add total deaths parameter
                    statement.setLong(10, stateData.totalDeaths)
                    // add new deaths parameter
                    statement.setLong(11, stateData.newDeaths)
                    // add total active cases parameter
                    statement.setLong(12, stateData.totalActiveCases)
                    // addstate id parameter
                    statement.setInt(13, stateId)
                    // execute statement
                    statement.execute()
                    // close statement
                    statement.close()
                }
                case None => {}
            }  
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Add states data error: " + se.getMessage())
            }
        }
    }

    /**
      * Method to add user data to users table
      *
      * @param connIn to database
      * @param user name
      * @param offset of query
      * @return success or failure
      */
    def addUsersData(connIn: Connection, user:String, offset: Int = 0): Boolean = {
        // move connection to a mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to insert user data to users table
            val statement = conn.prepareStatement("INSERT INTO users VALUES(DEFAULT, ?, " +
              "DEFAULT, ?);")
            // add user's name parameter
            statement.setString(1, user)
            // add query offset parameter
            statement.setInt(2, offset)
            // execute statement
            val isAdded = statement.execute()
            // close statement
            statement.close()
            isAdded
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Add states data error: " + se.getMessage())
                false
            }
        }
    }

    /**
      * Method to get state id from the states table
      *
      * @param connIn to database
      * @param state to get id for
      * @return id
      */
    def getStateId(connIn: Connection, state: String): Option[Int] = {
        // create option variable
        var stateId: Option[Int] = None
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to get state id from states table
            val statement = conn.prepareStatement("SELECT state_id FROM states WHERE state = ?;")
            // set state name parameter
            statement.setString(1, state)
            // execute query
            statement.executeQuery()
            // get result set
            val results = statement.getResultSet()
            if (results.next()) {
                // get result
                stateId = Some(results.getInt("state_id"))
            }
            stateId
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Get State id error: " + se.getMessage())
                stateId
            }
        }
    }

    /**
      * Method to update user's data in users table
      *
      * @param connIn to database
      * @param name of user
      * @param offset forQueries
      * @return success or failure
      */
    def updateUserData(connIn: Connection, name: String, offset: Int): Boolean = {
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to update user's data in users table
            val statement = conn.prepareStatement("UPDATE users SET state_offset = ? " +
              "WHERE name = ?;")
            // set user's query offset parameter
            statement.setInt(1, offset)
            // set user name parameter
            statement.setString(2, name)
            // execute statement
            val isUpdated = statement.execute()
            isUpdated
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Get State id error: " + se.getMessage())
                false
            }
        }
    }

    /**
      * Method to see if user's name is in users table
      *
      * @param name to check
      * @return success or failure
      */
    def checkUserName(connIn: Connection, name: String) : Boolean = {
        // create results variable
        var isThere = false
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        try {
            // create prepared statement to check if user's name is in users table
            val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM users " +
              "WHERE name = ?);")
            // set user's name parameter
            statement.setString(1, name)
            // execute query
            statement.executeQuery()
            // get result set
            val results = statement.getResultSet()
            // close statement
            statement.close()
            if (results.next()) {
                // get result
                isThere = results.getBoolean("EXISTS")
            }
            isThere
        } catch {
            // catch sql connection
            case se: SQLException => {
                println("Check user name error: " + se.getMessage())
                false
            }
        }
    }
}