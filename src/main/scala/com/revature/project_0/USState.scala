package com.revature.project_0


import scalaz._, Scalaz._
import argonaut._, Argonaut._
import ArgonautScalaz._

/**
 * Class for us states
 */ 
case class USState(var state: String, var population: Long, var populationUSARank: Int, 
    var pcOfUSAPopulation: Double, var mortalityRate: Double, 
    var pcOfUSADeaths: Double, var pcOfUSAActiveCases: Double, 
    var pcOfUSARecovered: Double, var pcOfUSATotalCases : Double,
    var totalCases: Long, var newCases: Long, var totalDeaths: Long, var newDeaths: Long, 
    var totalActiveCases: Long
    ) {

        implicit def USStateCodecJson: CodecJson[USState] =
            casecodec14(USState.apply, USState.unapply)("state", "population", "populationUSARank", 
            "pcOfUSAPopulation", "mortalityRate", "pcOfUSADeaths", "pcOfUSAActiveCases", 
            "pcOfUSARecovered", "pcOfUSATotalCases", "totalCases", "newCases", "totalDeaths", 
            "newDeaths", "totalActiveCases")
        
        // implicit val stateWrites: Writes[State] = (
        //     (JsPath \ "state").write[String] and
        //     (JsPath \ "population").write[Long] and
        //     (JsPath \ "populationUSARank").write[Int] and
        //     (JsPath \ "pcOfUSAPopulation").write[Double] and
        //     (JsPath \ "mortalityRate").write[Double] and
        //     (JsPath \ "pcOfUSADeaths").write[Double] and
        //     (JsPath \ "pcOfUSAActiveCases").write[Double] and
        //     (JsPath \ "pcOfUSARecovered").write[Double] and
        //     (JsPath \ "pcOfUSATotalCases").write[Double] and
        //     (JsPath \ "totalCases").write[Long] and
        //     (JsPath \ "newCases").write[Long] and
        //     (JsPath \ "totalDeaths").write[Long] and
        //     (JsPath \ "newDeaths").write[Long] and
        //     (JsPath \ "totalActiveCases").write[Long]
        // )(unlift(Location.unapply))
        

        // /**
        //   * implicit read a state class from a json object
        //   *
        //   * @param jsonState to read from
        //   * @return state class
        //   */
        // implicit val stateReads: Reads[State] = (
        //     (JsPath \ "state").read[String] and
        //     (JsPath \ "population").read[Long] and
        //     (JsPath \ "populationUSARank").read[Int] and
        //     (JsPath \ "pcOfUSAPopulation").read[Double] and
        //     (JsPath \ "mortalityRate").read[Double] and
        //     (JsPath \ "pcOfUSADeaths").read[Double] and
        //     (JsPath \ "pcOfUSAActiveCases").read[Double] and
        //     (JsPath \ "pcOfUSARecovered").read[Double] and
        //     (JsPath \ "pcOfUSATotalCases").read[Double] and
        //     (JsPath \ "totalCases").read[Long] and
        //     (JsPath \ "newCases").read[Long] and
        //     (JsPath \ "totalDeaths").read[Long] and
        //     (JsPath \ "newDeaths").read[Long] and
        //     (JsPath \ "totalActiveCases").read[Long]
        // )(State.apply _)
    }