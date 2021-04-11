package com.revature.project_0

object Main extends App {
  val db = new DatabaseUtil()
  val conn = db.getConnection()
  println(db.disconnect(conn.get))
}