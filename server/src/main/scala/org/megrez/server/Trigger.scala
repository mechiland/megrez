package org.megrez.server

import actors.Actor
import org.megrez.util.Workspace
import collection.mutable.HashMap
import java.io.File

trait Trigger {
  val pipeline: Pipeline
  val target: Actor

  def start

  def stop

  def checkAndTrigger = {
    if (pipeline.checkChange()) {
      target ! new TriggerBuild(pipeline)
    }
  }
}

class ChangeSet(val pipeline: org.megrez.Pipeline, val workspace: Workspace) {
  private val current = HashMap[org.megrez.Material, Option[Any]]()
  pipeline.materials.foreach(current.put(_, None))

  def get = {
    var changesFound = false
    for (material <- pipeline.materials) {
      val folder = workspace.createFolder(pipeline.name + (if (material.destination == "$main") "" else "/" + material.destination))
      material.source.changes(folder, current(material)) match {
        case workset : Some[Any] =>
          current.put(material, workset)
          changesFound = true
        case _ =>
      }
    }
    if (changesFound) Some(current.toMap) else None
  }
}