package org.megrez.server.trigger

import actors.Actor
import java.util.UUID
import collection.mutable.{HashSet, HashMap}
import org.megrez.util.Logging
import org.megrez.{Pipeline, Job}

trait Trigger {
  def start

  def stop
}












