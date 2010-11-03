package org.megrez

import java.util.UUID

case class JobAssignment(val build : UUID, val materials : Map[Material, Option[Any]], val job : Job)