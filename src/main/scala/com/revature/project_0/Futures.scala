package com.revature.project_0

import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Success
import scala.util.Failure
import java.sql.SQLException

object Futures {

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
        case Success(value) => value
        case Failure(exception) => println(s"Something went wrong setting up tables ${exception.getMessage()}")
      }
    })
  }

  def doSetup() = {
    // declare control variable
    var isCreated = false
    val stateString = FileUtil.getTextContent("states.json")
    val stateDataMaybe = JSONUtil.getStateList(stateString)
    stateDataMaybe match {
      case Some(stateDataList) => {
        val dbUtil = new DatabaseUtil()
        val conn = dbUtil.getConnection()
        dbUtil.createTables(conn.get, stateDataList)
      }
      case None => {}
    }
  }
}