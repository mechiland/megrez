package org.megrez.server.model.vcs

import org.neo4j.graphdb.Node
import org.megrez.server.model.data.Plugin
import org.megrez.server.model.ChangeSource

class Git private (val node : Node) extends ChangeSource with org.megrez.model.vcs.Git {
  val url = read(Git.url)
}

object Git extends Plugin(ChangeSource, "git") {
  val url = property[String]("url")

  def apply(node : Node) = new Git(node)
}