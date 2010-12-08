package org.megrez.server.model.tasks

import org.megrez.server.model.data.Plugin
import org.megrez.server.model.Task
import org.neo4j.graphdb.Node

class Ant private (val node : Node) extends Task {
  val target = read(Ant.target)
  val buildFile = read(Ant.buildFile)


  def toTask = new org.megrez.task.AntTask(target, buildFile)
}

object Ant extends Plugin(Task, "ant") {
  val target = property[String]("target")
  val buildFile = property[String]("buildfile")

  def apply(node : Node) = new Ant(node)
}