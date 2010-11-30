package org.megrez.server.model

import data.{Meta, Entity}
import org.neo4j.graphdb.{Node, DynamicRelationshipType}
import java.io.File

class Material private(val node: Node) extends Entity with org.megrez.model.Material {
  val destination = read(Material.destination)
  val changeSource = read(Material.changeSource)
  val lastChange = reader(Material.lastChange)

  def getChange(workingDir: File): Option[Change] = {
    val dir = Option(destination).map(new File(workingDir, _)).getOrElse(workingDir)
    changeSource.getChange(dir, this).map {
      change =>
        write(Material.lastChange, change)
        change
    }
  }
}

object Material extends Meta[Material] {
  val destination = property[String]("destination")
  val changeSource = reference("source", ChangeSource, DynamicRelationshipType.withName("SOURCE"))
  val lastChange = reference("lastChange", Change, DynamicRelationshipType.withName("LAST_CHANGE"))

  def apply(node: Node) = new Material(node)
}