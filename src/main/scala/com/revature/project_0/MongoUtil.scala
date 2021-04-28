package com.revature.project_0

import org.mongodb.scala.MongoClient
import org.mongodb.scala.MongoDatabase
import org.mongodb.scala.MongoCollection
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.Document
import scala.util.parsing.json.JSONArray
import scala.concurrent.Future


/**
* Class to deal with mongo database
*/
class MongoUtil {
  implicit val ec = scala.concurrent.ExecutionContext.global
  
  /**
    * Method to get mongo db connection
    *
    * @return mongo db client
    */
  def getConnection(): MongoClient = {
    // get environment user name
    val user = sys.env("USER_NAME")
    // get environment password
    val password = sys.env("USER_PASSWORD")
    // build the connection string
    val uri: String = s"mongodb+srv://$user:$password@cluster0.jasj1.mongodb.net/covid?retryWrites=true&w=majority"
    // set mongodb async type
    System.setProperty("org.mongodb.async.type", "netty")
    // cet the connection
    val client: MongoClient = MongoClient(uri)
    client
  }

  /**
    * Method to get the database in mongo
    *
    * @param client to the mongo cluster
    * @return mongo cluster database
    */
  def getDatabase(client: MongoClient): MongoDatabase = {
    
    // get the covid database
    val db: MongoDatabase = client.getDatabase("covid")
    db
  }

  /**
    * Method to put states into states collection
    * Non Blocking
    */
  def initialSetup(): Unit = {
    // get mongo class
    val mongoUtil = new MongoUtil();
    // connect to mongo cluster
    val client = mongoUtil.getConnection()
    // get database
    val db = mongoUtil.getDatabase(client)
    // get collection
    val collection = db.getCollection("states")
    // Declare variable
    var count: Long = 0
    // get document count
    val checkCollection = collection
      .countDocuments()
      .toFuture()
      // make sure data is received
    Await.ready(checkCollection, Duration.apply(1L, "second")).onComplete {
      case Success(result) => {
        count = result
        if (count == 0) {
          // get json string from file
          val stateString = FileUtil.getTextContent("states.json")
          //convert json string to a list of US States
          val stateDataMaybe = JSONUtil.getStateList(stateString)
          stateDataMaybe match {
            // process success
            case Some(stateDataList) => {
              for (state <- stateDataList) {
                val document = Document(
                  "state" -> state.state,
                  "population" -> state.population,
                  "populationUSARank" -> state.populationUSARank,
                  "pcOfUSAPopulation" -> state.pcOfUSAPopulation,
                  "mortalityRate" -> state.mortalityRate,
                  "pcOfUSADeaths" -> state.pcOfUSADeaths,
                  "pcOfUSAActiveCases" -> state.pcOfUSAActiveCases,
                  "pcOfUSARecovered" -> state.pcOfUSARecovered,
                  "pcOfUSATotalCases" -> state.pcOfUSATotalCases,
                  "totalCases" -> state.totalCases,
                  "newCases" -> state.newCases,
                  "totalDeaths" -> state.totalDeaths,
                  "newDeaths" -> state.newDeaths,
                  "totalActiveCases" -> state.totalActiveCases
                )
                val insertDoc = collection.insertOne(document).toFuture()
                Await.ready(insertDoc, Duration.apply(1L, "second")).onComplete {
                  case Success(result) => result
                  case Failure(e) => println("\nInsert Failure: " + e.toString)
                }
              }
            }
            case None => {
              // Process failure
              println ("Failed to process json")
              System.exit(0)
            }
          }
        }
      }
      case Failure(e) =>
        println("\nFailure: " + e.toString)
    }
    client.close()
   }

   /**
     * Method to ad user data to mongo database
     * Non blocking
     * @param user name to add
     * @param db to use
     */
   def addUserData(user: String, db: MongoDatabase): Unit = {
      // get collection
      val collection = db.getCollection("users")
      // create a user document
      val document = Document("name" -> user, 
        "lastUpdated" -> java.time.LocalDate.now().toString(), 
        "stateOffset" -> 0,
        "percentOffset" -> 0)
      // insert document
      val insertDoc = collection.insertOne(document).toFuture()
      // wait for completion
      Await.ready(insertDoc, Duration.apply(1L, "second")).onComplete {
        case Success(result) => {}
        case Failure(e) => println("\nInsert Failure: " + e.toString)
      }
   }

   /**
     * Method to add user to the mongo database
     * Blocking 
     * @param name of user
     * @param db to use
     * @return user document
     */
   def getUserData(name: String, db: MongoDatabase): Future[Document] = {
     // get collection
     val collection = db.getCollection("users")
     // check name
     val checkUser = collection
      .find(equal("name", name)).first()
      .toFuture()
      // make sure data is received
      Await.ready(checkUser, Duration.apply(1L, "second"))
   }

   /**
     * Method to get count of state documents in states collection
     * blocking
     * @param db to use
     * @return number of documents
     */
   def getStateCount(db: MongoDatabase): Future[Long] = {
     // get collection
     val collection = db.getCollection("states")
     // get document count
    val getCount = collection
      .countDocuments()
      .toFuture()
      // make sure data is received
    Await.ready(getCount, Duration.apply(1L, "second"))
   }

   /**
     * Method to get a sequence of states basic information from the mongo database
     * blocking
     * @param db to use
     * @param stateOffset for the user
     * @param limit of records to get
     * @return sequence of state documents
     */
   def getStateSeq(db: MongoDatabase, stateOffset: Int, limit: Int): 
    Future[Seq[Document]] = {
      // get collection
      val collection = db.getCollection("states")
      // get states
      val getStates = collection.find()
        .projection(include("state", "population", "populationUSARank", 
        "totalCases", "newCases", "totalDeaths", "newDeaths", "totalActiveCases"))
        .sort(ascending("state")).skip(stateOffset).limit(limit).toFuture()
        // wait for results
      Await.ready(getStates, Duration.apply(2L, "second"))
   }

   /**
     * Method to get a sequence of states percent information from the mongo database
     * Blocking
     * @param db to use
     * @param stateOffset of the user
     * @param limit of records to get
     * @return sequence of state documents
     */
   def getPercentSeq(db: MongoDatabase, stateOffset: Int, limit: Int): 
    Future[Seq[Document]] = {
      // get collection
      val collection = db.getCollection("states")
      // get states
      val getStates = collection.find()
        .projection(include("state", "pcOfUSAPopulation", "mortalityRate", 
        "pcOfUSADeaths", "pcOfUSAActiveCases", "pcOfUSARecovered", 
        "pcOfUSATotalCases"))
        .sort(ascending("state")).skip(stateOffset).limit(limit).toFuture()
        // wait for results
      Await.ready(getStates, Duration.apply(2L, "second"))
   }

   /**
     * Method to update the users information
     * Non Blocking
     * @param user information to update
     * @param db to use
     */
   def updateUserData(user: User, db: MongoDatabase): Unit = {
      // get collection
      val collection = db.getCollection("users")
      // update user document
      val updateDoc = collection.updateOne(equal("name", user.name), 
        combine(set("lastVisited", user.lastVisited),
        set("stateOffset", user.stateOffset), 
        set("percentOffset", user.percentOffset))).toFuture()
        // wait for results
      Await.ready(updateDoc, Duration.apply(1L, "second")).onComplete {
        case Success(result) => {}
        case Failure(e) => println("\nUpdate Failure: " + e.toString)
      }
   }
}