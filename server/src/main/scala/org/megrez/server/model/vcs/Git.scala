package org.megrez.server.model.vcs

import org.neo4j.graphdb.Node
import org.megrez.server.model.data.Plugin
import java.io.File
import org.megrez.server.model.{Material, Change, ChangeSource}

class Git private (val node : Node) extends ChangeSource with org.megrez.model.vcs.Git {
  val url = read(Git.url)

  def getChange(workingDir: File, material : Material): Option[Change] = None
}

object Git extends Plugin(ChangeSource, "git") {
  val url = property[String]("url")

  def apply(node : Node) = new Git(node)
}