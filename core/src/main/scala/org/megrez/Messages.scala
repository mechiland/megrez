package org.megrez

case class JobAssignment(val pipeline : String, val materials : Map[Material, Option[Any]], val job : Job)

case class JobFailed(val reason : String)
case class JobCompleted

object Stop