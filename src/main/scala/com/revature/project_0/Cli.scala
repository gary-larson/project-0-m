package com.revature.project_0

import scala.io.StdIn
import scala.util.matching.Regex
import java.io.FileNotFoundException
import scala.collection.mutable.Map

/** CLI class, used to communicate with the user
  */
class Cli {
  // Declare constant
  val screenLimit = 10;

  /**
    * Entry point for Cli class
    */
  def run(): Unit = {

    // run initial setup on another thread
    Futures.initialSetup()
    
    var user = getUserName()
    // TODO make users menu loop
    var continueLoop = true
    // get database and connection
    val dbUtil = new DatabaseUtil
    val conn = dbUtil.getConnection()
    conn match {
      case Some(value) => {}
      case None => {
        println("Could not connect to database. Please correct and try again.")
        System.exit(1)
      }
    }
    while (continueLoop) {
      printMainMenu()
      // read a character from the user
      val input = StdIn.readChar()
      
      // process selection
      input match {
        case '1' => {
          // TODO print screen of basic data and update user in database
          val stateOffset = user.stateOffset
          val states = dbUtil.getStateArray(conn.get, stateOffset, screenLimit)
          states match {
            case None => {println("Error getting data")}
            case Some(value) => {
              println()
              println("               State  Population Population-Rank Total-Cases New-Cases " +
                "Total-Deaths New-Deaths Total-Active-Cases")
              for(state <- value) {
                println(f"${state.state}%20s ${state.population}%,11d " +
                  f"${state.populationUSARank}%,15d ${state.totalCases}%,11d " +
                  f"${state.newCases}%,9d ${state.totalDeaths}%,12d " +
                  f"${state.newDeaths}%,10d ${state.totalActiveCases}%,18d")
              }
            }
          }
          user.stateOffset = user.stateOffset + screenLimit
          val count = dbUtil.getStateCount(conn.get)
          count match {
            case None => {}
            case Some(value) => if (user.stateOffset > value) user.stateOffset = 0
          }
          dbUtil.updateUserData(conn.get, user.name, 
            user.stateOffset, user.percentOffset)
        }
        case '2' => {
          // TODO print screen of percent data and update user in database
          val percentOffset = user.percentOffset
          val states = dbUtil.getPercentStateArray(conn.get, percentOffset, 
            screenLimit)
          states match {
            case None => {println("Error getting data")}
            case Some(value) => {
              println()
              println("               State Percent-Population Mortality-Rate Percent-Cases " +
                "Percent-Deaths Percent-Recovered Percent-Active-Cases")
              for(state <- value) {
                println(f"${state.state}%20s ${state.pcOfUSAPopulation}%,18.2f " +
                  f"${state.mortalityRate}%,14.2f ${state.pcOfUSATotalCases}%,13.2f " +
                  f"${state.pcOfUSADeaths}%,14.2f ${state.pcOfUSARecovered}%,17.2f " +
                  f"${state.pcOfUSAActiveCases}%,20.2f")
              }
            }
          }
          user.percentOffset = user.percentOffset + screenLimit
          val count = dbUtil.getStateCount(conn.get)
          count match {
            case None => {}
            case Some(value) => if (user.percentOffset > value) user.percentOffset = 0
          }
          dbUtil.updateUserData(conn.get, user.name, 
            user.stateOffset, user.percentOffset)
        }
        case 'q' => {
          continueLoop = false
        }
        case 'Q' => {
          continueLoop = false
        }
        case _ => {
          println("Entry no valid please enter '1', '2', 'Q' or 'q'")
        }
      }
    }
    dbUtil.disconnect(conn.get)
  }

  /**
    * Method to print main menu
    */
  def printMainMenu(): Unit = {
    println()
    println("   *** MAIN MENU ***")
    println("press '1' to print next screen of basic state data")
    println("press '2' to print next screen of percent state data")
    println("Press 'q' or 'Q' to quit")
  }

  /**
    * Method to get user name based on action selected
    *
    * @return a user
    */
  def getUserName(): User = {
    // Declare control variable
    var repeatMenu = true
    // Create user option
    var userOption: Option[User] = None
    // start loop
    while (repeatMenu) {
      // print menu
      printUserMenu()
      // read a character from the user
      val input = StdIn.readChar()
      // process selection
      input match {
        case '1' => {
          userOption = getUser()
        }
        case '2' => {
          userOption = createUser()
        }
        case 'q' => {
          System.exit(0)
        }
        case 'Q' => {
          System.exit(0)
        }
        case _ => {
          println("Entry no valid please enter '1', '2', 'Q' or 'q'")
        }
      }
      userOption match {
        // process failure
        case None => {println("Try Again")}
        // process success
        case Some(value) => { repeatMenu = false}
      }
    }
    userOption.get
  }

  /**
    * Method to print the user menu
    */
  def printUserMenu(): Unit = {
    println()
    println("   *** USER MENU ***")
    println("Enter '1' for an existing user.")
    println("Enter '2' to create a new user.)")
    println("Enter 'q' or 'Q' to quit program.")
  }

  /**
    * Method to get the user
    *
    * @return user
    */
  def getUser(): Option[User] = {
    // Declare control variable
    var nameExists = false
    var notQuit = true
    // Declare name variable
    var name = ""
    // Declare a database variable
    val dbUtil = new DatabaseUtil()
    // get database connection
    val conn = dbUtil.getConnection()
    conn match {
      // if no connection to database terminate program
      case None => {
        println("Could not connect to database. Please correct and try again.")
        System.exit(1)
      }
      case Some(value) => {}
    }
    // setup loop
    while (!nameExists && notQuit) {
      // get name
      name = inputName()
      if (name == "q" || name == "Q") {
        notQuit = false
      } else {
        // check name
        if (dbUtil.checkUserName(conn.get, name)) {
          nameExists = true
        } else {
          // print error
          println(s"Name: $name does not exist. Please choose another.")
        } 
      }
    }
    var user: Option[User] = None
    if (notQuit) {
      // get user
      user = dbUtil.getUserData(conn.get, name)
    } 
    // disconnect connection
    dbUtil.disconnect(conn.get)
    user
  }

  /**
    * Method to create a new username
    *
    * @return a user
    */
  def createUser(): Option[User] = {
    // Declare control variable
    var nameExists = true
    var notQuit = true
    // Declare name variable
    var name = ""
    // Declare a database variable
    val dbUtil = new DatabaseUtil()
    // get database connection
    val conn = dbUtil.getConnection()
    conn match {
      // if no connection to database terminate program
      case Some(value) => {}
      case None => {
        println("Could not connect to database. Please correct and try again.")
        System.exit(1)
      }
    }
    // setup loop
    while (nameExists && notQuit) {
      // get name
      name = inputName()
      // check if user wants to quit
      if (name == "q" || name == "Q") {
        notQuit = false
      } else {
        // check name
        if (!dbUtil.checkUserName(conn.get, name)) {
          nameExists = false
        } else {
          // print error
          println(s"Name: $name does exists. Please choose another.")
        }
      }
    }
    var user: Option[User] = None
    if (notQuit) {
      // add user
      dbUtil.addUserData(conn.get, name)
      // get user
      user = dbUtil.getUserData(conn.get, name)
    }
    // disconnect connection
    dbUtil.disconnect(conn.get)
    user
  }

  /**
    * Method to input a name from the user
    *
    * @return a string with a valid name
    */
  def inputName(): String = {
    // Declare control variable
    var isInValidName = true
    // Declare name
    var name = ""
    // setup loop
    while (isInValidName) {
      // print message
      println("Enter a name (letters and numbers only)")
      println(" Press 'q' or 'Q' for previous menu")
      // get name from user
      name = StdIn.readLine()
      // set control variable
      var isValid = true
      for(character <- name) {
        // test for invalid characters
        if (!(character.isLetter || character.isDigit)) {
          isValid = false
        }
      }
      // test name valid
      if (isValid) {
        isInValidName = false
      }
    }
    name
  }
}