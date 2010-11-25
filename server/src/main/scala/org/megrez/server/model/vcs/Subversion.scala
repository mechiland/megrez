package org.megrez.server.model.vcs

import org.neo4j.graphdb.Node
import org.megrez.server.model.data.Plugin
import org.megrez.server.model.ChangeSource

class Subversion private (val node : Node) extends ChangeSource {
  val url = read(Subversion.url)
}

object Subversion extends Plugin(ChangeSource, "svn") {
  val url = property[String]("url")

  def apply(node : Node) = new Subversion(node)
}