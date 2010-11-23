package org.megrez.server.data

import org.neo4j.graphdb.Node

trait Task extends Entity

object Task extends Pluggable[Task]

class CommandLine private (val node : Node) extends Task {
}

object CommandLine extends Plugin[CommandLine](Task, "cmd") {
  def apply(node : Node) = new CommandLine(node)
}