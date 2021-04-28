package com.revature.project_0

import java.sql.Date

case class User (name: String, var lastVisited: String, 
  var stateOffset: Int, var percentOffset: Int) {
    //def this() = {this("", "", 0, 0)}
  }