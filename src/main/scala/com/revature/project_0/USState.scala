package com.revature.project_0

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
        def this() = {
            this("", 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0)
        }
    }