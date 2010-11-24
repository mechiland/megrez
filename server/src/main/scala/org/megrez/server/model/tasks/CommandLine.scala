package org.megrez.server.model.tasks

import org.megrez.server.model.data.Plugin
import org.megrez.server.model.Task
import org.neo4j.graphdb.Node

class CommandLine private (val node : Node) extends Task {
  val command = read(CommandLine.command)
}

object CommandLine extends Plugin(Task, "cmd") {
  val command = property[String]("command")

  def apply(node : Node) = new CommandLine(node)
}