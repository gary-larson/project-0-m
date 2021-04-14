package com.revature.project_0


import scalaz._, Scalaz._
import argonaut._, Argonaut._
import ArgonautScalaz._


object JSONUtil {

    implicit def USStateCodecJson: CodecJson[USState] =
            casecodec14(USState.apply, USState.unapply)("state", "population", "populationUSARank", 
            "pcOfUSAPopulation", "mortalityRate", "pcOfUSADeaths", "pcOfUSAActiveCases", 
            "pcOfUSARecovered", "pcOfUSATotalCases", "totalCases", "newCases", "totalDeaths", 
            "newDeaths", "totalActiveCases")

    def getStateList(stateString: String): Option[List[USState]] = {
        
        val option: Option[List[USState]] =
            Parse.decodeOption[List[USState]](stateString)
        option
    }
}