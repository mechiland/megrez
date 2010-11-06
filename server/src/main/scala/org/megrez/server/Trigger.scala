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
