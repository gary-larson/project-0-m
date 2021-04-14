package com.revature.project_0

import play.api.libs.json.JsString
import play.api.libs.json.JsObject
import org.checkerframework.checker.units.qual.s
import play.api.libs.json.JsPath
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber
import play.api.libs.json.JsValue

/**
 * Class for states
 */ 
case class State(var name: String, var population: Long, var totalCases: Long, 
    var newCases: Long, var totalDeaths: Long, var newDeaths: Long, 
    var totalActiveCases: Long, var populationRank: Int, 
    var percentPopulation: Double, var mortalityRate: Double, 
    var percentDeaths: Double, var percentActive: Double, 
    var percentCases: Double, var percentRecovered : Double) {

        /**
          * Method to write a state class to a json object
          *
          * @param state to transfer to json
          */
        def writeState(state: State) = {
            JsObject(Seq(
                "state" -> JsString(state.name),
                "population" -> JsNumber(state.population),
                "populationUSARank" -> JsNumber(state.populationRank),
                "pcOfUSAPopulation" -> JsNumber(state.percentPopulation),
                "mortalityRate" -> JsNumber(state.mortalityRate),
                "pcOfUSADeaths" -> JsNumber(state.percentDeaths),
                "pcOfUSAActiveCases" -> JsNumber(state.percentActive),
                "pcOfUSARecovered" -> JsNumber(state.percentRecovered),
                "pcOfUSATotalCases" -> JsNumber(state.percentCases),
                "totalCases" -> JsNumber(state.totalCases),
                "newCases" -> JsNumber(state.newCases),
                "totalDeaths" -> JsNumber(state.totalDeaths),
                "newDeaths" -> JsNumber(state.newDeaths),
                "totalActiveCases" -> JsNumber(state.totalActiveCases)
            ))
        }

        /**
          * Method to read a state class from a json object
          *
          * @param jsonState to read from
          * @return state class
          */
        def readState(jsonState: JsValue) = {
            val name = (jsonState \ "state").as[String]
            val population = (jsonState \ "population").as[Long]
            val populationRank = (jsonState \ "populationUSARank").as[Int]
            val percentPopulation = (jsonState \ "pcOfUSAPopulation").as[Double]
            val mortalityRate = (jsonState \ "mortalityRate").as[Double]
            val percentDeaths = (jsonState \ "pcOfUSADeaths").as[Double]
            val percentActiveCases = (jsonState \ "pcOfUSAActiveCases").as[Double]
            val percentRecovered = (jsonState \ "pcOfUSARecovered").as[Double]
            val percentTotalCases = (jsonState \ "pcOfUSATotalCases").as[Double]
            val totalCases = (jsonState \ "totalCases").as[Long]
            val newCases = (jsonState \ "newCases").as[Long]
            val totalDeaths = (jsonState \ "totalDeaths").as[Long]
            val newDeaths = (jsonState \ "newDeaths").as[Long]
            val totalActiveCases = (jsonState \ "totalActiveCases").as[Long]
            State(name, population, totalCases, newCases, totalDeaths, newDeaths, 
                totalActiveCases, populationRank, percentPopulation, mortalityRate, 
                percentDeaths, percentActive, percentCases, percentRecovered)
        }
    }