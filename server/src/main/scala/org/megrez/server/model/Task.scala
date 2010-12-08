package org.megrez.server.model

import data.{Pluggable, Entity}

trait Task extends Entity {
  def toTask : org.megrez.Task
}

object Task extends Pluggable[Task]