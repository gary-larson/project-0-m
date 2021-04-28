package com.revature.project_0

import java.sql.DriverManager
import java.sql.Connection
import java.sql.SQLException
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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
        val user = sys.env("USER_NAME")
        // hard coded password **BAD**
        val password = sys.env("USER_PASSWORD")
        // attempt connection
        val conn = DriverManager.getConnection(connectString, user, password)
        Some(conn)
    }

    /**
      * Method to disconnect connection
      *
      * @param conn to disconnect
      */
    def disconnect(conn: Connection): Unit = {
        if (!conn.isClosed()) {
            // close connection
            conn.close()
        }
    }

    /**
      * Method to see is all tables Exist
      *
      * @param connIn to database
      * @return success or failure
      */
    def checkTables(connIn: Connection): Boolean = {
        // Declare control Variable
        var allTables = true
        // move connection to mutable variable
        var conn = connIn;
        // test connection
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
       
        // create prepared statement to check the states table exists
        val statement1 = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
            "WHERE schemaname = 'public' AND tablename  = 'states');")
        // execute query
        statement1.executeQuery()
        // get result set
        val results1 = statement1.getResultSet()
        if (results1.next()) {
            // get results
            if(!results1.getBoolean("EXISTS")) {
                allTables = false
            }
        }
        // close statement
        statement1.close()
        // create prepared statement to check the state_info table exists
        val statement2 = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
            "WHERE schemaname = 'public' AND tablename  = 'state_info');")
        // execute query
        statement2.executeQuery()
        // get result set
        val results2 = statement2.getResultSet()
        if (results2.next()) {
            // get results
            if(!results2.getBoolean("EXISTS")) {
                allTables = false
            }
        }
        // close statement
        statement2.close()
        // create prepared statement to check the users table exists
        val statement3 = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
            "WHERE schemaname = 'public' AND tablename  = 'users');")
        // execute query
        statement3.executeQuery()
        // get result set
        val results3 = statement3.getResultSet()
        if (results3.next()) {
            // get results
            if(!results3.getBoolean("EXISTS")) {
                allTables = false
            }
        }
        // close statement
        statement3.close()
        allTables
    }

    /**
      * Method to create any missing tables
      *
      * @param connIn to database
      * @param stateData for states and state_info tables
      */
    def createTables(connIn: Connection, stateDataList: List[USState]): Unit = {
        // move connection to mutable variable
        var conn = connIn;
        // test connection
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement for states table
        val statement1 = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
            "WHERE schemaname = 'public' AND tablename  = 'states');")
            // execute statement
            statement1.executeQuery()
            // get result set
            val results1 = statement1.getResultSet()
            if (results1.next()) {
                // test results
                if (!results1.getBoolean("EXISTS")) {
                // create states table
                createStatesTable(conn, stateDataList)
                }
            }
            // close statement
            statement1.close()
        // create prepared statement for state info table
        val statement2 = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
            "WHERE schemaname = 'public' AND tablename  = 'state_info');")
        // execute query
        statement2.executeQuery()
        // get result set
        val results2 = statement2.getResultSet()
        if (results2.next()) {
            // test results
            if (!results2.getBoolean("EXISTS")) {
            // create state info table
            createStateInfoTable(conn, stateDataList)
            }
        }
        // close statement
        statement2.close()
        // create prepared statement for users table
        val statement3 = conn.prepareStatement("SELECT EXISTS (SELECT FROM pg_tables " +
            "WHERE schemaname = 'public' AND tablename  = 'users');")
        // execute query
        statement3.executeQuery()
        // get result set
        val results3 = statement3.getResultSet()
        if (results3.next()) {
            // test results
            if (!results3.getBoolean("EXISTS")) {
            // create table
            createUsersTable(conn)
            }
        }
        // close statement
        statement3.close()
    }

    /**
      * Method to create states table
      *
      * @param connIn to database
      * @param stateDataList for states and state_info tables
      */
    def createStatesTable(connIn: Connection, stateDataList: List[USState]) : Unit = {
        // move connection to a mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        
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
    }

    /**
      * Method to create states info table
      *
      * @param connIn to database
      * @param stateDataList for states and state_info tables
      */
    def createStateInfoTable(connIn: Connection, stateDataList: List[USState]) : Unit = {
        // move connection to a mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        
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
    }

    /**
      * Method to create users table
      *
      * @param connIn to database
      */
    def createUsersTable(connIn: Connection) : Unit = {
        // move variable to mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement to create users table
        val statement = conn.prepareStatement("CREATE TABLE users (user_id serial " +
            "primary key, name text UNIQUE, last_updated date DEFAULT now(), " +
            "state_offset int DEFAULT 0, percent_offset int DEFAULT 0);")
        // execute statement
        statement.execute()
        // close statement
        statement.close()
    }

    /**
      * Method to add state data to states table
      *
      * @param connIn to database
      * @param stateData to add
      */
    def addStatesData(connIn: Connection, stateData: USState): Unit = {
        // move connection to mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        
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
    }

    /**
      * Method to add state info to state_info table
      *
      * @param connIn to database
      * @param stateData to add
      */
    def addStateInfoData(connIn: Connection, stateData: USState): Unit = {
        // move connection to mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
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
    }

    /**
      * Method to add user data to users table
      *
      * @param connIn to database
      * @param user name
      * @param offset of query
      */
    def addUserData(connIn: Connection, user: String, stateOffset: Int = 0, 
      percentOffset: Int = 0): Unit = {
        // move connection to a mutable variable
        var conn = connIn;
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement to insert user data to users table
        val statement = conn.prepareStatement("INSERT INTO users VALUES(DEFAULT, ?, " +
            "DEFAULT, ?, ?);")
        // add user's name parameter
        println(user)
        statement.setString(1, user)
        // add query state offset parameter
        statement.setInt(2, stateOffset)
        // add percent offset parameter
        statement.setInt(3, percentOffset)
        // execute statement
        statement.execute()
        // close statement
        statement.close()
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
    }

    /**
      * Method to get state id from the states table
      *
      * @param connIn to database
      * @param state to get id for
      * @return id
      */
    def getStateCount(connIn: Connection): Option[Int] = {
        // create option variable
        var count: Option[Int] = None
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement to get state id from states table
        val statement = conn.prepareStatement("SELECT COUNT(state_id) AS count FROM states;")
        // execute query
        statement.executeQuery()
        // get result set
        val results = statement.getResultSet()
        if (results.next()) {
            // get result
            count = Some(results.getInt("count"))
        }
        count
    }

    /**
      * Method to get basic state data from database
      *
      * @param connIn to database
      * @param stateOffset for sql sarting point
      * @param stateLimit the number of results
      * @return array buffer of us states
      */
    def getStateArray(connIn: Connection, stateOffset: Int, stateLimit: Int): 
        Option[ArrayBuffer[USState]] = {
        // create option variable
        var state: Option[ArrayBuffer[USState]] = None
        val states =  ArrayBuffer[USState]()
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement to get state id from states table
        val statement = conn.prepareStatement("SELECT state, population, populationUSARank, " +
            "totalCases, newCases, totalDeaths, newDeaths, totalActiveCases FROM states " +
            "INNER JOIN state_info ON states.state_id = state_info.state_id  ORDER BY state " +
            "LIMIT ? OFFSET ?;")
        // add limit parameter
        statement.setInt(1, stateLimit)
        // add offset parameter
        statement.setInt(2, stateOffset)
        // execute query
        statement.executeQuery()
        // get result set
        val results = statement.getResultSet()
        while (results.next()) {
            // get result
            val newState = new USState()
            // get state
            newState.state = results.getString("state")
            // get population
            newState.population = results.getInt("population")
            // get population rank
            newState.populationUSARank = results.getInt("populationUSARank")
            // get total cases
            newState.totalCases = results.getInt("totalCases")
            // get new cases
            newState.newCases = results.getInt("newCases")
            // get total deaths
            newState.totalDeaths = results.getInt("totalDeaths")
            // get new deaths
            newState.newDeaths = results.getInt("newDeaths")
            // get total active cases
            newState.totalActiveCases = results.getInt("totalActiveCases")
            // add new state to states
            states += newState
        }
        // put states in an option
        state = new Some(states)
        state
    }

    /**
      * Method to get percent state data from database
      *
      * @param connIn to database
      * @param percentOffset for sql sarting point
      * @param percentLimit the number of results
      * @return array buffer of us states
      */
    def getPercentStateArray(connIn: Connection, percentOffset: Int, percentLimit: Int): 
        Option[ArrayBuffer[USState]] = {
        // create option variable
        var state: Option[ArrayBuffer[USState]] = None
        val states =  ArrayBuffer[USState]()
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement to get state id from states table
        val statement = conn.prepareStatement("SELECT state, pcOfUSAPopulation, " +
            "mortalityRate, pcOfUSADeaths, pcOfUSAActiveCases, pcOfUSARecovered, " +
            "pcOfUSATotalCases FROM states INNER JOIN state_info ON states.state_id = " +
            "state_info.state_id  ORDER BY state LIMIT ? OFFSET ?;")
        // add limit parameter
        statement.setInt(1, percentLimit)
        // add offset parameter
        statement.setInt(2, percentOffset)
        // execute query
        statement.executeQuery()
        // get result set
        val results = statement.getResultSet()
        while (results.next()) {
            // get result
            val newState = new USState()
            // get state
            newState.state = results.getString("state")
            // get population
            newState.pcOfUSAPopulation = results.getDouble("pcOfUSAPopulation")
            // get population rank
            newState.mortalityRate = results.getDouble("mortalityRate")
            // get total cases
            newState.pcOfUSADeaths = results.getDouble("pcOfUSADeaths")
            // get new cases
            newState.pcOfUSAActiveCases = results.getDouble("pcOfUSAActiveCases")
            // get total deaths
            newState.pcOfUSARecovered = results.getDouble("pcOfUSARecovered")
            // get new deaths
            newState.pcOfUSATotalCases = results.getDouble("pcOfUSATotalCases")
            // add new state to states
            states += newState
        }
        // put states in an option
        state = new Some(states)
        state
    }

    /**
      * Method to get an option of a user
      *
      * @param connIn to database
      * @param name of user to get
      * @return option of user
      */
    def getUserData(connIn: Connection, name: String): Option[User] = {
      // create option variable
      var userOption: Option[User] = None
      // move connection to a mutable variable
      var conn = connIn
      if (conn.isClosed()) {
          // get connection
          conn = getConnection().get
      }
      // create prepared statement to get state id from states table
      val statement = conn.prepareStatement("SELECT name, last_updated, " +
        "state_offset, percent_offset FROM users WHERE name = ?;")
      // set state name parameter
      statement.setString(1, name)
      // execute query
      statement.executeQuery()
      // get result set
      val results = statement.getResultSet()
      if (results.next()) {
        // get results
        val name = results.getString("name")
        val lastUpdated = results.getString("last_updated")
        val stateOffset = results.getInt("state_offset")
        val percentOffset = results.getInt("percent_offset")
        val user = User(name, lastUpdated, stateOffset, percentOffset)
        userOption = Some(user)
      }
      userOption
    }

    /**
      * Method to update user's data in users table
      *
      * @param connIn to database
      * @param name of user
      * @param offset forQueries
      */
    def updateUserData(connIn: Connection, name: String, stateOffset: Int, 
      percentOffset: Int): Unit = {
        // move connection to a mutable variable
        var conn = connIn
        if (conn.isClosed()) {
            // get connection
            conn = getConnection().get
        }
        // create prepared statement to update user's data in users table
        val statement = conn.prepareStatement("UPDATE users SET state_offset = ?, " +
          "percent_offset = ?, last_updated = default WHERE name = ?;")
        // set user's basic query offset parameter
        statement.setInt(1, stateOffset)
        // set user's query percent offset parameter
        statement.setInt(2, percentOffset)
        // set user name parameter
        statement.setString(3, name)
        // execute statement
        statement.execute()
        // close statement
        statement.close()
    }

    /**
      * Method to see if user's name is in users table
      *
      * @param name to check
      * @return exists or not
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
        // create prepared statement to check if user's name is in users table
        val statement = conn.prepareStatement("SELECT EXISTS (SELECT FROM users " +
            "WHERE name = ?);")
        // set user's name parameter
        statement.setString(1, name)
        // execute query
        statement.executeQuery()
        // get result set
        val results = statement.getResultSet()
        if (results.next()) {
            // get result
            isThere = results.getBoolean("EXISTS")
        }
        // close statement
        statement.close()
        isThere
    }
}