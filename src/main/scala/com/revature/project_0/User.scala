package com.revature.project_0

import java.sql.Date

case class User (name: String, var lastVisited: Date, 
  var stateOffset: Int, var percentOffset: Int) 