package org.megrez.server.model.vcs

import org.megrez.server.model.data.Plugin
import java.io.File
import org.megrez.server.model.{Material, Change, ChangeSource}
import org.neo4j.graphdb.{DynamicRelationshipType, Node}

class Subversion private(val node: Node) extends ChangeSource with org.megrez.model.vcs.Subversion {
  val url = read(Subversion.url)

  def getChange(workingDir: File, material : Material): Option[Change] =
    Option(material.lastChange()) match {
      case Some(r: Subversion.Revision) =>
        changes(workingDir, Some(r.revision)).map(r => Subversion.Revision(Map("revision" -> r, "metarial" -> material)))
      case None =>
        changes(workingDir, None).map(r => Subversion.Revision(Map("revision" -> r, "metarial" -> material)))
    }
}

object Subversion extends Plugin(ChangeSource, "svn") {
  val url = property[String]("url")

  def apply(node: Node) = new Subversion(node)

  class Revision private(val node: Node) extends Change {
    val revision = read(Revision.revision)
    val material = read(Revision.material)
  }

  object Revision extends Plugin(Change, "svn_revision") {
    val revision = property[Int]("revision")
    val material = reference("metarial", Material, DynamicRelationshipType.withName("FROM"))

    def apply(node: Node) = new Revision(node)
  }
}