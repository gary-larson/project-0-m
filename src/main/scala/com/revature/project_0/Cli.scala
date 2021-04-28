package com.revature.project_0

import scala.io.StdIn
import scala.util.matching.Regex
import java.io.FileNotFoundException
import scala.collection.mutable.Map
import org.mongodb.scala._
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.util.hashing.Hashing

/** CLI class, used to communicate with the user
  */
class Cli {
  // Declare constant
  val screenLimit = 10;

  /**
    * Entry point for Cli class
    */
  def run(): Unit = {
    // check for data
    val mongoUtil = new MongoUtil()
    mongoUtil.initialSetup()
    // connect to mongo cluster
    val client = mongoUtil.getConnection()
    // get database
    val db = mongoUtil.getDatabase(client)
       
    
    
    // get name from user
    var user = getUserName(db)
    // TODO make users menu loop
    var continueLoop = true
    while (continueLoop) {
      printMainMenu(user.name)
      // read a character from the user
      val charRead = StdIn.readChar()
      
      // process selection
      charRead match {
        case '1' => {
          // print screen of basic data and update user in database
          // get state offset
          val stateOffset = user.stateOffset
          // get states from database
          val states = mongoUtil.getStateSeq(db, stateOffset, screenLimit)
          // match return value
          states.value match {
            // get result
            case Some(result) => {
              // match result
              result match {
                // in case of success print to console
                case Success(value) => {
                  println()
                  println("               State  Population Population-Rank Total-Cases New-Cases " +
                    "Total-Deaths New-Deaths Total-Active-Cases")
                    // loop through sequence
                  for(document <- value) {
                    // print each document
                    println(f"${document.getString("state")}%20s ${document.getLong("population")}%,11d " +
                      f"${document.getInteger("populationUSARank")}%,15d ${document.getLong("totalCases")}%,11d " +
                      f"${document.getLong("newCases")}%,9d ${document.getLong("totalDeaths")}%,12d " +
                      f"${document.getLong("newDeaths")}%,10d ${document.getLong("totalActiveCases")}%,18d")
                  }
                }
                // catch exception
                case Failure(exception) => println (s"Error!: ${exception.getMessage()}")
              }
            }
            // if none query never finished
            case None => println("\nSomething went wrong Try again!")
          }
          // update user state offset
          user.stateOffset = user.stateOffset + screenLimit
          // get count of state documents
          val count = mongoUtil.getStateCount(db)
          count.value match {
            case Some(result) => {
              result match {
                // failure catch exception
                case Failure(exception) => {println(s"Error: ${exception.getMessage}")}
                // for success adjust state offset if needed
                case Success(value) => if (user.stateOffset > value) user.stateOffset = 0
              }
            }
            case None => println("Something went wrong please try again!")
          }
          // update user in database
          mongoUtil.updateUserData(user, db)
        }
        case '2' => {
          // print screen of percent data and update user in database
          val percentOffset = user.percentOffset
          // get states percent data
          val states = mongoUtil.getPercentSeq(db, percentOffset, screenLimit)
          // mathc value
          states.value match {
            // if result process
            case Some(result) => {
              // match result
              result match {
                // for success print documents to console
                case Success(value) => {
                  println()
                  println("               State Percent-Population Mortality-Rate Percent-Cases " +
                    "Percent-Deaths Percent-Recovered Percent-Active-Cases")
                    // loop through state documents
                  for(document <- value) {
                    // print state documents
                    println(f"${document.getString("state")}%20s ${document.getDouble("pcOfUSAPopulation")}%,18.2f " +
                      f"${document.getDouble("mortalityRate")}%,14.2f ${document.getDouble("pcOfUSATotalCases")}%,13.2f " +
                      f"${document.getDouble("pcOfUSADeaths")}%,14.2f ${document.getDouble("pcOfUSARecovered")}%,17.2f " +
                      f"${document.getDouble("pcOfUSAActiveCases")}%,20.2f")
                  }
                }
                // if failure catch exception
                case Failure(exception) => println (s"Error!: ${exception.getMessage()}")
              }
            }
            // if none query never completed
            case None => println("\nSomething went wrong Try again!")
          }
          // update user's percent offset
          user.percentOffset = user.percentOffset + screenLimit
          // get count of state documents
          val count = mongoUtil.getStateCount(db)
          // get results
          count.value match {
            case Some(result) => {
              result match {
                // if failure catch exception
                case Failure(exception) => {println(s"Error: ${exception.getMessage}")}
                // update percent offset if needed
                case Success(value) => if (user.percentOffset > value) user.percentOffset = 0
              }
            }
            // if none query never completed
            case None => println("Something went wrong please try again!")
          }
          // update user in database
          mongoUtil.updateUserData(user, db)
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
    // close database connection
    client.close()
  }

  /**
    * Method to print main menu
    */
  def printMainMenu(name: String): Unit = {
    println()
    println(s"Welcome $name")
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
  def getUserName(db: MongoDatabase): User = {
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
          userOption = getUser(db)
        }
        case '2' => {
          userOption = createUser(db)
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
  def getUser(db: MongoDatabase): Option[User] = {
    // get collection
    val collection = db.getCollection("users") 
    // Declare control variable
    var nameExists = false
    var notQuit = true
    // Declare name variable
    var name = ""
    // Declare a database variable
    val mongoUtil = new MongoUtil()
    // create final variable
    var user: Option[User] = None
    // setup loop
    while (!nameExists && notQuit) {
      // get name
      name = inputName()
      if (name == "q" || name == "Q") {
        notQuit = false
      } else {
        // check if user exists
        val checkUser = mongoUtil.getUserData(name, db)
        checkUser.value match {
          case Some(result) => {
            result match {
              case Success(value) => {
                if (value == null) {
                  // name does not exist
                  println(s"Name: $name does not exist. Please choose another.")
                } else {
                  // check for key
                  if(value.contains("name")) {
                    // test for match
                    if (value.getString("name").toLowerCase() == name.toLowerCase()) {
                      // set control variable and get user
                      nameExists = true
                      user = Some(User(value.getString("name"), 
                        value.getString("lastUpdated"), 
                        value.getInteger("stateOffset"),
                        value.getInteger("percentOffset")))
                    } else {
                      // name does not exist
                      println(s"Name: $name does not exist. Please choose another.")
                    }
                  } else {
                    // name does not exist
                    println(s"Name: $name does not exist. Please choose another.")
                  }
                }
              }
              // if failure catch exception
              case Failure(exception) => 
                println(s"Failure!: ${exception.getMessage()}")
            }
            
          }
          // if none query never completed
           case None =>
            println("\nSomething went wrong Try again!")
        }
      }
    }
    user
  }

  /**
    * Method to create a new username
    *
    * @return a user
    */
  def createUser(db: MongoDatabase): Option[User] = {
    // get collection
    val collection = db.getCollection("users") 
    // Declare control variable
    var nameExists = true
    var notQuit = true
    // Declare name variable
    var name = ""
    // Declare a database variable
    val mongoUtil = new MongoUtil()
    // setup loop
    while (nameExists && notQuit) {
      // get name
      name = inputName()
      // check if user wants to quit
      if (name == "q" || name == "Q") {
        notQuit = false
      } else {
        // check user
        val checkUser = mongoUtil.getUserData(name, db)
        checkUser.value match {
          case Some(result) => {
            result match {
              case Success(value) => {
                if (value == null) {
                  // name does not exist
                  nameExists = false
                } else {
                  if(value.contains("name")) {
                    // test name
                    if (value.getString("name").toLowerCase() == name.toLowerCase()) {
                      // name exists
                      println(s"Name: $name does exist. Please choose another.")
                    } else {
                      // name does not exist
                      nameExists = false
                    }
                  } else {
                    // name does not exist
                    nameExists = false
                  }
                }
              }
              // if failed catch exception
              case Failure(exception) => 
                println(s"Failure!: ${exception.getMessage()}")
            }
          }
          // if none query never completed
           case None =>
            println("\nSomething went wrong Try again!")
        }
      }
    }
    var user: Option[User] = None
    if (notQuit) {
      // add user
      val userTemp = User(name, "", 0, 0)
      mongoUtil.addUserData(name, db)
      // get user
      user = Some(userTemp)
    }
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