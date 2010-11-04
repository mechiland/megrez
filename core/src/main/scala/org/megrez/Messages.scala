package org.megrez

case class JobAssignment(val pipeline : String, val materials : Map[Material, Option[Any]], val job : Job)

case class JobCompleted