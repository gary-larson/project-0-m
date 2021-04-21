package com.revature.project_0

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Success
import scala.util.Failure
import java.sql.SQLException

/**
  * Object to run threads
  */
object Futures {

  /**
    * Method to run the initial setup on the database
    */
  def initialSetup(): Unit = {
    // to use Futures, we need to declare an implicit ExecutionContext (don't worry much about it)
    implicit val ec = scala.concurrent.ExecutionContext.global

    // start future and call setup
    val testTablesFuture = Future {
      doSetup()
    }

    // call back for test tables future
    testTablesFuture.onComplete((result) => {
      result match{
        // process success
        case Success(value) => value
        case Failure(exception) => {
          // process failure
          println(s"Error! Message: ${exception.getMessage()}")
          if (exception.getMessage().contains("TCP/IP connections.")) {
            println("Please correct and try again!")
            System.exit(0)
          }
        }
      }
    })
  }

  /**
    * Method to process setup
    */
  def doSetup() = {
    // declare control variable
    var isCreated = false
    // get json string from file
    val stateString = FileUtil.getTextContent("states.json")
    // convert json string to a list of US States
    val stateDataMaybe = JSONUtil.getStateList(stateString)
    stateDataMaybe match {
      // process success
      case Some(stateDataList) => {
        // get database and connection
        val dbUtil = new DatabaseUtil()
        val conn = dbUtil.getConnection()
        // create tables
        dbUtil.createTables(conn.get, stateDataList)
        // close connection
        dbUtil.disconnect(conn.get)
      }
      case None => {
        // Process failure
        println ("Failed to process json")
        System.exit(0)
      }
    }
  }
}