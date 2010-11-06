package org.megrez.server.trigger

import org.megrez.util.Workspace
import collection.mutable.HashMap
import org.megrez.{Pipeline, Material}

class Materials(val pipeline: Pipeline, previous: Map[Material, Option[Any]], val workspace: Workspace) {
  private val lastChanges = HashMap[Material, Option[Any]](previous.toSeq: _*)

  def this(pipeline: Pipeline, workspace: Workspace) = this (pipeline, pipeline.materials.map(_ -> None).toMap, workspace)

  def changes = lastChanges.toMap

  def hasChanges = {
    var changesFound = false
    for (material <- pipeline.materials) {
      val folder = workspace.createFolder(pipeline.name + (if (material.destination == "$main") "" else "/" + material.destination))
      material.source.changes(folder, lastChanges(material)) match {
        case workset: Some[Any] =>
          lastChanges.put(material, workset)
          changesFound = true
        case _ =>
      }
    }
    changesFound
  }
}